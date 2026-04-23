import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const API = "http://localhost:3000";
const CATEGORIES = [
  { value: "electrical", label: "Electrical", icon: "⚡" },
  { value: "plumbing", label: "Plumbing", icon: "🔧" },
  { value: "water", label: "Internet", icon: "🌐" },
  { value: "maintenance", label: "Mess / Food", icon: "🍽️" },
  { value: "cleanliness", label: "Cleanliness", icon: "🧹" },
  { value: "other", label: "Security", icon: "🔒" },
];

function DashBoard() {
  const [username, setUsername] = useState("");
  const [studentHostel, setStudentHostel] = useState("");
  const [studentRoomNo, setStudentRoomNo] = useState("");
  const [complaints, setComplaints] = useState([]);
  const [activeView, setActiveView] = useState("dashboard");
  const [expandedComplaintId, setExpandedComplaintId] = useState("");

  const [title, setTitle] = useState("");
  const [roomNo, setRoomNo] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState("low");
  const [category, setCategory] = useState("plumbing");
  const [urgent, setUrgent] = useState(false);

  const navigate = useNavigate();

  function capitalizeFirstLetter(str) {
    if (!str) return "";
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      navigate("/");
      return;
    }
    async function fetchData() {
      try {
        const userRes = await axios.get(`${API}/me`, {
          headers: { authorization: token },
        });
        setUsername(userRes.data.username);
        setStudentHostel(userRes.data.hostel || "");
        setStudentRoomNo(userRes.data.room_no || "");

        const compRes = await axios.get(`${API}/get-complaints`, {
          headers: { authorization: token },
        });
        setComplaints(compRes.data);
        if (compRes.data.length > 0) {
          setExpandedComplaintId(compRes.data[0]._id);
        }
      } catch (err) {
        console.error(err);
      }
    }
    fetchData();
  }, [navigate]);

  async function submitComplaint(e) {
    e.preventDefault();
    const token = localStorage.getItem("token");
    try {
      const res = await axios.post(
        `${API}/new-complaint`,
        {
          title: description ? `${title} - ${description}` : title,
          room_no: roomNo,
          category,
          urgent: priority === "high" || urgent,
        },
        { headers: { authorization: token } }
      );
      setComplaints([res.data, ...complaints]);
      setTitle("");
      setRoomNo("");
      setDescription("");
      setPriority("low");
      setCategory("plumbing");
      setUrgent(false);
      setActiveView("dashboard");
    } catch (err) {
      const msg = err.response?.data?.message;
      if (msg) alert(msg);
      else console.error(err);
    }
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    navigate("/");
  }

  const pendingComplaints = complaints.filter((item) => !item.done);
  const resolvedComplaints = complaints.filter((item) => item.done);
  const inProgressCount = pendingComplaints.length > 0 ? 1 : 0;

  function getCategoryLabel(val) {
    return CATEGORIES.find((c) => c.value === val)?.label || val;
  }

  function getStatus(item, index) {
    if (item.done) return "Resolved";
    return index === 0 ? "In progress" : "Open";
  }

  function getComplaintCode(item) {
    if (!item?._id) return "CMP-0000";
    return `CMP-${item._id.slice(-4).toUpperCase()}`;
  }

  function getComplaintStatus(item) {
    if (!item) return "submitted";
    if (item.status) return item.status;
    if (item.done) return "resolved";
    return "submitted";
  }

  function getProgressStep(item) {
    const status = getComplaintStatus(item);
    if (status === "resolved") return 4;
    if (status === "assigned") return 3;
    if (status === "open") return 2;
    return 1;
  }

  function formatStatusLabel(status) {
    if (status === "assigned") return "Assigned";
    if (status === "open") return "Open";
    if (status === "resolved") return "Resolved";
    return "In progress";
  }

  return (
    <div className="dashboard student-v2">
      <div className="student-shell">
        <aside className="student-sidebar">
          <div className="student-brand">
            <h3>HostelDesk</h3>
            <p>
              {studentHostel || "Hostel"}
              {studentRoomNo ? ` — Room ${studentRoomNo}` : ""}
            </p>
          </div>
          <div className="student-menu">
            <button
              className={`student-menu-item ${activeView === "dashboard" ? "active" : ""}`}
              onClick={() => setActiveView("dashboard")}
            >
              Dashboard
            </button>
            <button
              className={`student-menu-item ${activeView === "new" ? "active" : ""}`}
              onClick={() => setActiveView("new")}
            >
              New complaint
            </button>
            <button
              className={`student-menu-item ${activeView === "my" ? "active" : ""}`}
              onClick={() => setActiveView("my")}
            >
              My complaints
            </button>
            <button className="student-menu-item">Notifications</button>
            <button className="student-menu-item">Settings</button>
            <button className="student-menu-item danger" onClick={logout}>
              Logout
            </button>
          </div>
        </aside>

        <main className="student-main">
          {activeView === "new" && (
            <div className="student-form-wrap student-form-modern">
              <div className="student-header">
                <h1>Submit a complaint</h1>
                <p>Select a category and describe the issue</p>
              </div>
              {!studentHostel && (
                <p className="form-hint" style={{ color: "#fca5a5" }}>
                  Your hostel is not set. Please contact admin.
                </p>
              )}
              <form onSubmit={submitComplaint}>
                <div className="form-group">
                  <label>Category</label>
                  <div className="category-grid-modern">
                    {CATEGORIES.map((c) => (
                      <button
                        key={c.value}
                        type="button"
                        className={`category-tile ${category === c.value ? "active" : ""}`}
                        onClick={() => setCategory(c.value)}
                      >
                        <span>{c.icon}</span>
                        {c.label}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="form-group">
                  <label>Title</label>
                  <input
                    type="text"
                    placeholder="Ceiling fan not working"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                  />
                </div>
                <div className="student-form-row">
                  <div className="form-group">
                    <label>Room number</label>
                    <input
                      type="text"
                      placeholder="214"
                      value={roomNo}
                      onChange={(e) => setRoomNo(e.target.value)}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Priority</label>
                    <select
                      value={priority}
                      onChange={(e) => setPriority(e.target.value)}
                    >
                      <option value="low">Low</option>
                      <option value="medium">Medium</option>
                      <option value="high">High</option>
                    </select>
                  </div>
                </div>
                <div className="form-group">
                  <label>Description</label>
                  <textarea
                    rows={4}
                    placeholder="The ceiling fan has been making a grinding noise..."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                  />
                </div>
                <div className="student-form-actions">
                  <button type="submit" className="student-new-btn" disabled={!studentHostel}>
                    Submit complaint →
                  </button>
                  <button type="button" className="student-cancel-btn" onClick={() => setActiveView("dashboard")}>
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}

          {activeView === "dashboard" && (
            <>
              <div className="student-header">
                <h1>Good morning, {capitalizeFirstLetter(username)} 👋</h1>
                <p>Here&apos;s what&apos;s happening with your hostel</p>
              </div>

              <div className="student-stats">
                <div className="student-stat-card">
                  <span>Open</span>
                  <strong className="open">{pendingComplaints.length}</strong>
                </div>
                <div className="student-stat-card">
                  <span>In progress</span>
                  <strong className="progress">{inProgressCount}</strong>
                </div>
                <div className="student-stat-card">
                  <span>Resolved</span>
                  <strong className="resolved">{resolvedComplaints.length}</strong>
                </div>
              </div>

              <div className="student-recent">
                <h2>Recent complaints</h2>
                {complaints.length === 0 ? (
                  <div className="empty-state">
                    <span className="empty-icon">📋</span>
                    <p>No complaints yet.</p>
                  </div>
                ) : (
                  complaints.map((item, index) => (
                    <div
                      key={item._id}
                      className={`student-complaint-card ${item.urgent ? "urgent" : ""}`}
                    >
                      <div>
                        <h4>{item.title}</h4>
                        <p>
                          Room {item.room_no} • {getCategoryLabel(item.category)} •{" "}
                          {new Date(item.createdAt).toLocaleDateString()}
                        </p>
                        <p>
                          {item.urgent
                            ? "Marked urgent by student."
                            : "Complaint submitted and waiting for action."}
                        </p>
                      </div>
                      <span className={`status-badge ${getStatus(item, index).toLowerCase().replace(" ", "-")}`}>
                        {getStatus(item, index)}
                      </span>
                    </div>
                  ))
                )}
                <button className="student-new-btn" onClick={() => setActiveView("new")}>
                  + New complaint
                </button>
              </div>
            </>
          )}

          {activeView === "my" && (
            <div className="my-complaints-view">
              {complaints.length > 0 ? (
                <>
                  <div className="my-complaint-head">
                    <h2>My complaints</h2>
                    <p>Click a complaint to expand and view live status progress.</p>
                  </div>

                  <div className="my-complaint-list-accordion">
                    {complaints.map((item) => {
                      const isExpanded = expandedComplaintId === item._id;
                      const step = getProgressStep(item);
                      const status = getComplaintStatus(item);
                      return (
                        <div key={item._id} className="my-complaint-accordion-item">
                          <button
                            className="my-complaint-accordion-header"
                            onClick={() =>
                              setExpandedComplaintId((prev) => (prev === item._id ? "" : item._id))
                            }
                          >
                            <div>
                              <strong>Complaint #{getComplaintCode(item)}</strong>
                              <p>
                                {item.title} — {getCategoryLabel(item.category)}
                              </p>
                            </div>
                            <span className={`status-badge ${status === "resolved" ? "resolved" : status === "open" ? "open" : "in-progress"}`}>
                              {formatStatusLabel(status)}
                            </span>
                          </button>

                          {isExpanded && (
                            <div className="my-complaint-accordion-body">
                              <div className="complaint-progress compact">
                                <div className="step">
                                  <span className={`dot ${step >= 1 ? "done" : ""}`}>1</span>
                                  <small>Submitted</small>
                                </div>
                                <span className="line" />
                                <div className="step">
                                  <span className={`dot ${step >= 2 ? "done" : ""}`}>2</span>
                                  <small>Open</small>
                                </div>
                                <span className="line" />
                                <div className="step">
                                  <span className={`dot ${step >= 3 ? "done" : ""}`}>3</span>
                                  <small>Assigned</small>
                                </div>
                                <span className="line" />
                                <div className="step">
                                  <span className={`dot ${step >= 4 ? "done" : ""}`}>4</span>
                                  <small>Resolved</small>
                                </div>
                              </div>
                              <div className="status-panel compact">
                                <div>
                                  <h3>Current status</h3>
                                  <p>
                                    Assigned to <strong>{item.assignedStaff || "Not assigned yet"}</strong>
                                  </p>
                                  <p>
                                    Submitted: <strong>{new Date(item.createdAt).toLocaleString()}</strong>
                                  </p>
                                </div>
                              </div>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </>
              ) : (
                <div className="empty-state">
                  <span className="empty-icon">📋</span>
                  <p>No complaints found.</p>
                </div>
              )}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default DashBoard;
