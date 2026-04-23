import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const API = "http://localhost:3000";
const CATEGORIES = [
  { value: "plumbing", label: "Plumbing" },
  { value: "electrical", label: "Electrical" },
  { value: "cleanliness", label: "Cleanliness" },
  { value: "water", label: "Water Supply" },
  { value: "maintenance", label: "Maintenance" },
  { value: "other", label: "Other" },
];

function DashBoardAdmin() {
  const [wardenName, setWardenName] = useState("");
  const [hostel, setHostel] = useState("");
  const [complaints, setComplaints] = useState([]);
  const [byCategory, setByCategory] = useState({});
  const [activeTab, setActiveTab] = useState("all");
  const navigate = useNavigate();

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
        if (userRes.data.role !== "warden") {
          localStorage.removeItem("token");
          navigate("/");
          return;
        }
        setWardenName(userRes.data.username);
        setHostel(userRes.data.hostel || "");

        const compRes = await axios.get(`${API}/warden/complaints`, {
          headers: { authorization: token },
        });
        setComplaints(compRes.data.complaints || []);
        setByCategory(compRes.data.byCategory || {});
      } catch (err) {
        console.error(err);
      }
    }
    fetchData();
  }, [navigate]);

  async function markAsDone(id) {
    try {
      await axios.put(`${API}/markedDone/${id}`, {}, {
        headers: { authorization: localStorage.getItem("token") },
      });
      setComplaints((prev) =>
        prev.map((item) => (item._id === id ? { ...item, done: true } : item))
      );
      setByCategory((prev) => {
        const next = {};
        Object.keys(prev).forEach((cat) => {
          next[cat] = prev[cat].map((c) =>
            c._id === id ? { ...c, done: true } : c
          ).filter((c) => !c.done);
        });
        return next;
      });
    } catch (err) {
      console.error(err);
    }
  }

  async function updateStatus(id, status, assignedStaff) {
    try {
      const res = await axios.put(
        `${API}/warden/complaints/${id}/status`,
        { status, assignedStaff },
        { headers: { authorization: localStorage.getItem("token") } }
      );
      const updated = res.data;
      setComplaints((prev) =>
        prev.map((item) => (item._id === id ? { ...item, ...updated } : item))
      );
      setByCategory((prev) => {
        const next = {};
        Object.keys(prev).forEach((cat) => {
          next[cat] = prev[cat].map((c) => (c._id === id ? { ...c, ...updated } : c));
        });
        return next;
      });
    } catch (err) {
      console.error(err);
    }
  }

  function getCategoryLabel(val) {
    return CATEGORIES.find((c) => c.value === val)?.label || val;
  }

  const listToShow =
    activeTab === "all"
      ? complaints
      : byCategory[activeTab] || [];

  const stats = {
    total: complaints.length,
    urgent: complaints.filter((c) => c.urgent).length,
    byCat: Object.fromEntries(
      CATEGORIES.map((c) => [c.value, (byCategory[c.value] || []).length])
    ),
  };

  return (
    <div className="dashboard">
      <div className="dashboard-header-warden">
        <div>
          <h1 className="welcome">Welcome, {wardenName || "Warden"}</h1>
          <p className="dashboard-sub">
            Warden • {hostel} • Pending: {stats.total}
          </p>
        </div>
        <button className="btn danger logout-btn" onClick={() => { localStorage.removeItem("token"); localStorage.removeItem("role"); navigate("/"); }}>
          🔓 Logout
        </button>
      </div>

      <div className="warden-stats">
        <div className="stat-card">
          <span className="stat-value">{stats.total}</span>
          <span className="stat-label">Total</span>
        </div>
        <div className="stat-card urgent">
          <span className="stat-value">{stats.urgent}</span>
          <span className="stat-label">Urgent</span>
        </div>
      </div>

      <div className="card warden-card">
        <h2>Complaints by Category</h2>

        <div className="category-tabs">
          <button
            className={`cat-tab ${activeTab === "all" ? "active" : ""}`}
            onClick={() => setActiveTab("all")}
          >
            All
          </button>
          {CATEGORIES.map((c) => (
            <button
              key={c.value}
              className={`cat-tab ${activeTab === c.value ? "active" : ""}`}
              onClick={() => setActiveTab(c.value)}
            >
              {c.label}
              {stats.byCat[c.value] > 0 && (
                <span className="tab-count">{stats.byCat[c.value]}</span>
              )}
            </button>
          ))}
        </div>

        <div className="recent-complaints-content">
          {listToShow.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">✅</span>
              <p>
                {activeTab === "all"
                  ? "No pending complaints in your hostel."
                  : `No ${getCategoryLabel(activeTab)} complaints.`}
              </p>
            </div>
          ) : (
            listToShow.map((item) => (
              <div
                key={item._id}
                className={`complaint-item ${item.urgent ? "urgent" : ""}`}
              >
                <div>
                  <span className="category-badge">
                    {getCategoryLabel(item.category)}
                  </span>
                  <h4>{item.title}</h4>
                  <p>
                    {item.hostel}, Room {item.room_no}
                    {item.userId?.username && (
                      <> • by {item.userId.username}</>
                    )}
                  </p>
                  <p>Status: Pending</p>
                </div>
                <div className="item-actions">
                  {item.urgent && <span className="urgent-badge">Urgent</span>}
                  <button
                    className="status-action-btn"
                    onClick={() =>
                      updateStatus(item._id, "assigned", item.assignedStaff || "Hostel Staff")
                    }
                  >
                    Assign
                  </button>
                  <button
                    className="status-action-btn"
                    onClick={() => updateStatus(item._id, "open", item.assignedStaff || "Hostel Staff")}
                  >
                    Open
                  </button>
                  <label className="checkbox">
                    <input
                      type="checkbox"
                      checked={item.done}
                      onChange={() => markAsDone(item._id)}
                    />
                    Mark done
                  </label>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default DashBoardAdmin;
