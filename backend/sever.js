const express = require("express");
const jwt = require("jsonwebtoken");
const cookieParser = require("cookie-parser");
const mongoose = require("mongoose");
const bcrypt = require("bcrypt");
const z = require("zod");
const cors = require("cors");
const { GoogleGenerativeAI } = require("@google/generative-ai");

const genAI = new GoogleGenerativeAI("AIzaSyAX1J2rYbc1xI-pTxGxRfmy43doQ_HOm7g");
const app = express();
const JWT_SECRET = "terimaakichut";
const JWT_SECRET_WARDEN = "nai batao gya";

app.use(cors());
app.use(express.json());
app.use(cookieParser());

const { UserModel, ComplaintModel } = require("./db");

// Shared constants (for API consistency)
const HOSTELS = ["A-Block", "B-Block", "C-Block", "D-Block", "Girls Hostel"];
const CATEGORIES = ["plumbing", "electrical", "cleanliness", "internet", "maintenance", "other"];
const COMPLAINT_STATUSES = ["submitted", "assigned", "open", "resolved"];

mongoose
  .connect("mongodb://localhost:27017/new-db-for-project")
  .then(() => console.log("mongodb connected successfully"))
  .catch((err) => console.log("connection error", err));

// ================= SIGNUP =================
// Student signup - hostel required (complaints auto-linked to student's hostel)
app.post("/signup/student", async function (req, res) {
  try {
    const { username, password, hostel, room_no } = req.body;
    const requiredBody = z.object({
      username: z.string().min(3).max(100),
      password: z.string().min(3).max(12),
      hostel: z.string().min(1),
      room_no: z.string().min(1),
    });
    const parsedData = requiredBody.safeParse(req.body);
    if (!parsedData.success) {
      return res.status(400).json({ message: "Invalid format. Hostel is required." });
    }

    if (!HOSTELS.includes(hostel)) {
      return res.status(400).json({ message: "Invalid hostel" });
    }

    const existingUser = await UserModel.findOne({ username });
    if (existingUser) {
      return res.status(400).json({ message: "Username already exists" });
    }

    const hashedPassword = await bcrypt.hash(password, 5);
    await UserModel.create({
      username,
      password: hashedPassword,
      role: "student",
      hostel,
      room_no,
    });
    const token = jwt.sign({ username, role: "student" }, JWT_SECRET);
    return res.status(200).json({ token });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// Warden signup - requires hostel
app.post("/signup/warden", async function (req, res) {
  try {
    const { username, password, hostel } = req.body;
    const requiredBody = z.object({
      username: z.string().min(3).max(100),
      password: z.string().min(3).max(12),
      hostel: z.string().min(1),
    });
    const parsedData = requiredBody.safeParse(req.body);
    if (!parsedData.success) {
      return res.status(400).json({ message: "Invalid format. Hostel is required." });
    }

    if (!HOSTELS.includes(hostel)) {
      return res.status(400).json({ message: "Invalid hostel" });
    }

    const existingUser = await UserModel.findOne({ username });
    if (existingUser) {
      return res.status(400).json({ message: "Username already exists" });
    }

    const hashedPassword = await bcrypt.hash(password, 5);
    await UserModel.create({
      username,
      password: hashedPassword,
      role: "warden",
      hostel,
    });
    const token = jwt.sign({ username, role: "warden", hostel }, JWT_SECRET_WARDEN);
    return res.status(200).json({ token });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

// ================= SIGNIN =================
app.post("/signin/student", async function (req, res) {
  const { username, password } = req.body;
  try {
    const existingUser = await UserModel.findOne({ username, role: "student" });
    if (!existingUser) {
      return res.status(401).json({ message: "Invalid credentials" });
    }
    const passMatch = await bcrypt.compare(password, existingUser.password);
    if (!passMatch) {
      return res.status(401).json({ message: "Invalid credentials" });
    }
    const token = jwt.sign({ username, role: "student" }, JWT_SECRET);
    return res.status(200).json({ token });
  } catch (error) {
    return res.status(401).json({ message: "Invalid credentials" });
  }
});

app.post("/signin/warden", async function (req, res) {
  const { username, password } = req.body;
  try {
    const existingUser = await UserModel.findOne({ username, role: "warden" });
    if (!existingUser) {
      return res.status(401).json({ message: "Invalid credentials" });
    }
    const passMatch = await bcrypt.compare(password, existingUser.password);
    if (!passMatch) {
      return res.status(401).json({ message: "Invalid credentials" });
    }
    const token = jwt.sign(
      { username, role: "warden", hostel: existingUser.hostel },
      JWT_SECRET_WARDEN
    );
    return res.status(200).json({ token });
  } catch (error) {
    return res.status(401).json({ message: "Invalid credentials" });
  }
});

// ================= AUTH MIDDLEWARES =================
function authStudent(req, res, next) {
  const token = req.headers.authorization;
  if (!token) {
    return res.status(401).json({ message: "Please sign in first" });
  }
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.username = decoded.username;
    req.role = decoded.role;
    next();
  } catch (err) {
    return res.status(401).json({ message: "Invalid token" });
  }
}

function authWarden(req, res, next) {
  const token = req.headers.authorization;
  if (!token) {
    return res.status(401).json({ message: "Please sign in first" });
  }
  try {
    const decoded = jwt.verify(token, JWT_SECRET_WARDEN);
    req.username = decoded.username;
    req.role = "warden";
    req.wardenHostel = decoded.hostel;
    next();
  } catch (err) {
    return res.status(401).json({ message: "Invalid token" });
  }
}

// ================= ROUTES =================
app.get("/hostels", (req, res) => {
  res.json(HOSTELS);
});

app.get("/categories", (req, res) => {
  res.json(CATEGORIES);
});

app.get("/me", async (req, res) => {
  const token = req.headers.authorization;
  if (!token) {
    return res.status(401).json({ message: "Please sign in first" });
  }
  try {
    let decoded;
    try {
      decoded = jwt.verify(token, JWT_SECRET);
    } catch {
      decoded = jwt.verify(token, JWT_SECRET_WARDEN);
    }
    const user = await UserModel.findOne({ username: decoded.username });
    if (!user) {
      return res.status(401).json({ message: "User not found" });
    }
    res.json({
      username: user.username,
      role: user.role,
      hostel: user.hostel,
      room_no: user.room_no,
    });
  } catch (err) {
    return res.status(401).json({ message: "Invalid token" });
  }
});

app.post("/new-complaint", authStudent, async function (req, res) {
  try {
    const user = await UserModel.findOne({ username: req.username });
    const { title, urgent, category } = req.body;

    if (!category || !CATEGORIES.includes(category)) {
      return res.status(400).json({ message: "Invalid category" });
    }
    if (!user.hostel || !HOSTELS.includes(user.hostel)) {
      return res.status(400).json({ message: "Your hostel is not set. Please contact admin." });
    }

    let assignedStaff = null;
    if (category === "electrical") assignedStaff = "Electrical Dept";
    else if (category === "plumbing") assignedStaff = "Plumbing Team";
    else if (category === "internet") assignedStaff = "IT Support";
    else if (category === "cleanliness") assignedStaff = "Housekeeping";
    else if (category === "maintenance") assignedStaff = "Maintenance Team";

    const complaint = await ComplaintModel.create({
      userId: user._id,
      title,
      category,
      urgent: !!urgent,
      hostel: user.hostel,
      room_no: user.room_no,
      assignedStaff,
      status: "open",
      done: false,
    });
    return res.status(201).json(complaint);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

app.post("/ai/analyze-complaint", authStudent, async (req, res) => {
  try {
    const { title, description } = req.body;
    if (!title && !description) return res.json({ category: "other", priority: "low" });

    const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" });
    const prompt = `
      Analyze this hostel complaint and return a JSON object with 'category' and 'priority'.
      Title: ${title}
      Description: ${description}

      Categories: plumbing, electrical, cleanliness, internet, maintenance, other.
      Priority: low, medium, high. (High if it's an emergency, flooding, sparks, safety hazard, etc.)
      
      Respond with ONLY the raw JSON object (no markdown, no backticks). Format:
      {"category": "...", "priority": "..."}
    `;

    const result = await model.generateContent(prompt);
    let text = result.response.text().trim();
    if (text.startsWith("\`\`\`json")) text = text.slice(7, -3).trim();
    if (text.startsWith("\`\`\`")) text = text.slice(3, -3).trim();

    const parsed = JSON.parse(text);
    if (!CATEGORIES.includes(parsed.category)) parsed.category = "other";
    
    res.json(parsed);
  } catch (error) {
    console.error("AI Error:", error);
    res.json({ category: "other", priority: "low" });
  }
});

app.get("/get-complaints", authStudent, async function (req, res) {
  try {
    const user = await UserModel.findOne({ username: req.username });
    const complaints = await ComplaintModel.find({ userId: user._id }).sort({
      createdAt: -1,
    });
    res.json(complaints);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Warden: complaints for their hostel only, grouped by category
app.get("/warden/complaints", authWarden, async function (req, res) {
  try {
    const complaints = await ComplaintModel.find({
      hostel: req.wardenHostel,
      done: false,
    })
      .sort({ urgent: -1, createdAt: -1 })
      .populate("userId", "username")
      .lean();

    const byCategory = {};
    CATEGORIES.forEach((c) => (byCategory[c] = []));
    complaints.forEach((c) => {
      if (byCategory[c.category]) byCategory[c.category].push(c);
    });

    res.json({
      hostel: req.wardenHostel,
      complaints,
      byCategory,
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.put("/markedDone/:id", authWarden, async (req, res) => {
  try {
    const complaint = await ComplaintModel.findById(req.params.id);
    if (!complaint) {
      return res.status(404).json({ message: "Complaint not found" });
    }
    if (complaint.hostel !== req.wardenHostel) {
      return res.status(403).json({ message: "Not your hostel" });
    }
    await ComplaintModel.findByIdAndUpdate(req.params.id, {
      done: true,
      status: "resolved",
    });
    res.json({ message: "Marked as done" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.put("/warden/complaints/:id/status", authWarden, async (req, res) => {
  try {
    const { status, assignedStaff } = req.body;
    if (!COMPLAINT_STATUSES.includes(status)) {
      return res.status(400).json({ message: "Invalid status" });
    }

    const complaint = await ComplaintModel.findById(req.params.id);
    if (!complaint) {
      return res.status(404).json({ message: "Complaint not found" });
    }
    if (complaint.hostel !== req.wardenHostel) {
      return res.status(403).json({ message: "Not your hostel" });
    }

    const update = {
      status,
      done: status === "resolved",
    };
    if (typeof assignedStaff === "string") {
      update.assignedStaff = assignedStaff.trim() || null;
    }

    const updated = await ComplaintModel.findByIdAndUpdate(req.params.id, update, {
      new: true,
    });
    res.json(updated);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.listen(3000, () => {
  console.log("Server running on port 3000");
});
