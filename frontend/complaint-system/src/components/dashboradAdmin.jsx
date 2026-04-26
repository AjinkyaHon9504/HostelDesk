import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const API = "http://localhost:3000";
const CATEGORIES = [
  { value: "electrical", label: "Elec", color: "#93c5fd" },
  { value: "plumbing", label: "Plumb", color: "#fcd34d" },
  { value: "internet", label: "Net", color: "#86efac" },
  { value: "maintenance", label: "Mess", color: "#fca5a5" },
  { value: "cleanliness", label: "Clean", color: "#e5e7eb" },
  { value: "other", label: "Sec", color: "#fde68a" },
];

function DashBoardAdmin() {
  const [wardenName, setWardenName] = useState("");
  const [hostel, setHostel] = useState("");
  const [complaints, setComplaints] = useState([]);
  const [activeSection, setActiveSection] = useState("overview");
  const [selectedStatusId, setSelectedStatusId] = useState("");
  const [nextStatus, setNextStatus] = useState("submitted");
  const [statusRemark, setStatusRemark] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
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
        if ((compRes.data.complaints || []).length > 0) {
          setSelectedStatusId(compRes.data.complaints[0]._id);
        }
      } catch (err) {
        console.error(err);
      }
    }
    fetchData();
  }, [navigate]);

  const stats = {
    total: complaints.length,
    open: complaints.filter((c) => c.status === "open").length,
    assigned: complaints.filter((c) => c.status === "assigned").length,
    submitted: complaints.filter((c) => (c.status || "submitted") === "submitted").length,
    resolved: complaints.filter((c) => c.status === "resolved" || c.done).length,
    urgent: complaints.filter((c) => c.urgent).length,
    byCat: Object.fromEntries(CATEGORIES.map((c) => [c.value, complaints.filter((x) => x.category === c.value).length])),
  };
  const urgentComplaints = complaints.filter((c) => c.urgent).slice(0, 2);
  const selectedStatusComplaint =
    complaints.find((c) => c._id === selectedStatusId) || complaints[0] || null;
  const filteredStatusComplaints = complaints.filter((c) => {
    const s = c.status || "submitted";
    return statusFilter === "all" ? true : s === statusFilter;
  });
  const statusCounts = {
    all: complaints.length,
    submitted: complaints.filter((c) => (c.status || "submitted") === "submitted").length,
    open: complaints.filter((c) => (c.status || "submitted") === "open").length,
    assigned: complaints.filter((c) => (c.status || "submitted") === "assigned").length,
    resolved: complaints.filter((c) => (c.status || "submitted") === "resolved").length,
  };

  async function updateComplaintStatus() {
    if (!selectedStatusComplaint || !nextStatus) return;
    try {
      const res = await axios.put(
        `${API}/warden/complaints/${selectedStatusComplaint._id}/status`,
        {
          status: nextStatus,
          assignedStaff: selectedStatusComplaint.assignedStaff || null,
          remark: statusRemark,
        },
        { headers: { authorization: localStorage.getItem("token") } }
      );
      const updated = res.data;
      setComplaints((prev) =>
        prev.map((item) => (item._id === updated._id ? { ...item, ...updated } : item))
      );
      setStatusRemark("");
    } catch (err) {
      console.error(err);
    }
  }

  function getCategoryClass(category) {
    return ["plumbing", "electrical", "internet", "maintenance", "cleanliness", "other"].includes(category)
      ? category
      : "other";
  }

  function getStatusClass(status) {
    if (status === "open") return "open";
    if (status === "assigned") return "assigned";
    if (status === "resolved") return "resolved";
    return "submitted";
  }

  return (
    <div className="admin-v2-page">
      <div className="admin-v2-shell">
        <aside className="admin-v2-sidebar">
          <div className="admin-v2-brand">
            <h3>HostelDesk</h3>
            <p>Admin panel</p>
            <p>{hostel || "Hostel not set"}</p>
          </div>
          <nav className="admin-v2-nav">
            <button
              className={`admin-v2-nav-item ${activeSection === "overview" ? "active" : ""}`}
              onClick={() => setActiveSection("overview")}
            >
              Overview
            </button>
            <button
              className={`admin-v2-nav-item ${activeSection === "all" ? "active" : ""}`}
              onClick={() => setActiveSection("all")}
            >
              All complaints
            </button>
            <button
              className={`admin-v2-nav-item ${activeSection === "status" ? "active" : ""}`}
              onClick={() => setActiveSection("status")}
            >
              Update status
            </button>
            <button
              className="admin-v2-nav-item danger"
              onClick={() => {
                localStorage.removeItem("token");
                localStorage.removeItem("role");
                navigate("/");
              }}
            >
              Logout
            </button>
          </nav>
        </aside>

        <main className="admin-v2-main">
          {activeSection === "overview" && (
            <>
              <div className="admin-v2-head">
                <h1>Overview</h1>
                <p>All hostel complaints at a glance</p>
              </div>

              <div className="admin-v2-stats">
                <div className="admin-v2-stat-card">
                  <span>Total</span>
                  <strong>{stats.total}</strong>
                </div>
                <div className="admin-v2-stat-card">
                  <span>Open</span>
                  <strong className="open">{stats.open}</strong>
                </div>
                <div className="admin-v2-stat-card">
                  <span>Assigned</span>
                  <strong className="assigned">{stats.assigned}</strong>
                </div>
                <div className="admin-v2-stat-card">
                  <span>Submitted</span>
                  <strong className="submitted">{stats.submitted}</strong>
                </div>
                <div className="admin-v2-stat-card">
                  <span>Resolved</span>
                  <strong className="resolved">{stats.resolved}</strong>
                </div>
              </div>

              <div className="admin-v2-grid">
                <section className="admin-v2-panel">
                  <h2>By category</h2>
                  <div className="admin-v2-category-row">
                    {CATEGORIES.map((cat) => (
                      <div key={cat.value} className="admin-v2-category-item">
                        <span style={{ background: cat.color }} />
                        <small>{cat.label}</small>
                      </div>
                    ))}
                  </div>
                  <div className="admin-v2-category-counts">
                    {CATEGORIES.map((cat) => (
                      <small key={cat.value}>{stats.byCat[cat.value]}</small>
                    ))}
                  </div>
                </section>

                <section className="admin-v2-panel">
                  <h2>Urgent complaints</h2>
                  {urgentComplaints.length === 0 ? (
                    <div className="admin-v2-urgent-card">
                      <p>No urgent complaints</p>
                    </div>
                  ) : (
                    urgentComplaints.map((item) => (
                      <div key={item._id} className="admin-v2-urgent-card">
                        <div className="admin-v2-urgent-top">
                          <strong>{item.title}</strong>
                          <span>Urgent</span>
                        </div>
                        <p>
                          {item.hostel || hostel} • {item.category} •{" "}
                          {new Date(item.createdAt).toLocaleDateString()}
                        </p>
                      </div>
                    ))
                  )}
                </section>
              </div>
            </>
          )}

          {activeSection === "all" && (
            <>
              <div className="admin-v2-head">
                <h1>All complaints</h1>
                <p>All complaints in your hostel</p>
              </div>

              <div className="admin-all-card">
                <div className="admin-all-header-row">
                  <span>Title</span>
                  <span>Student</span>
                  <span>Category</span>
                  <span>Room</span>
                  <span>Date</span>
                </div>
                <div className="admin-all-scroll">
                  {complaints.map((item) => (
                    <div key={item._id} className="admin-all-row">
                      <div className="admin-all-title">
                        <strong>{item.title}</strong>
                        {item.urgent && <em>Urgent</em>}
                      </div>
                      <span>{item.userId?.username || "student"}</span>
                      <span className={`admin-cat-badge ${getCategoryClass(item.category)}`}>
                        {item.category}
                      </span>
                      <span>{item.room_no || "-"}</span>
                      <span>{new Date(item.createdAt).toLocaleDateString()}</span>
                    </div>
                  ))}
                </div>
              </div>
            </>
          )}

          {activeSection === "status" && (
            <>
              <div className="admin-v2-head">
                <h1>Update status</h1>
                <p>Track complaint progress by updating its current status</p>
              </div>

              <div className="admin-status-filters">
                <button
                  className={`admin-status-filter-btn ${statusFilter === "all" ? "active" : ""}`}
                  onClick={() => setStatusFilter("all")}
                >
                  All <span>{statusCounts.all}</span>
                </button>
                <button
                  className={`admin-status-filter-btn ${statusFilter === "submitted" ? "active" : ""}`}
                  onClick={() => setStatusFilter("submitted")}
                >
                  Submitted <span>{statusCounts.submitted}</span>
                </button>
                <button
                  className={`admin-status-filter-btn ${statusFilter === "open" ? "active" : ""}`}
                  onClick={() => setStatusFilter("open")}
                >
                  Open <span>{statusCounts.open}</span>
                </button>
                <button
                  className={`admin-status-filter-btn ${statusFilter === "assigned" ? "active" : ""}`}
                  onClick={() => setStatusFilter("assigned")}
                >
                  Assigned <span>{statusCounts.assigned}</span>
                </button>
                <button
                  className={`admin-status-filter-btn ${statusFilter === "resolved" ? "active" : ""}`}
                  onClick={() => setStatusFilter("resolved")}
                >
                  Resolved <span>{statusCounts.resolved}</span>
                </button>
              </div>

              <div className="admin-status-layout">
                <div className="admin-status-list">
                  {filteredStatusComplaints.map((item) => (
                    <button
                      key={item._id}
                      className={`admin-status-card ${item._id === selectedStatusId ? "active" : ""}`}
                      onClick={() => {
                        setSelectedStatusId(item._id);
                        setNextStatus(item.status || "submitted");
                      }}
                    >
                      <div className="admin-status-card-head">
                        <strong>{item.title}</strong>
                        {item.urgent && <span>Urgent</span>}
                      </div>
                      <p>
                        {item.room_no || "-"} · {item.category}
                      </p>
                      <small className={`admin-status-text ${getStatusClass(item.status || "submitted")}`}>
                        {item.status || "submitted"}
                      </small>
                      {item.assignedStaff && <em>Assigned: {item.assignedStaff}</em>}
                    </button>
                  ))}
                </div>

                <div className="admin-status-form">
                  <h2>Update status</h2>

                  {selectedStatusComplaint ? (
                    <>
                      <div className="admin-status-selected">
                        <strong>{selectedStatusComplaint.title}</strong>
                        <p>
                          {selectedStatusComplaint.room_no || "-"} · {selectedStatusComplaint.category}
                        </p>
                      </div>
                      <label>Current status</label>
                      <select value={nextStatus} onChange={(e) => setNextStatus(e.target.value)}>
                        <option value="submitted">submitted</option>
                        <option value="open">open</option>
                        <option value="assigned">assigned</option>
                        <option value="resolved">resolved</option>
                      </select>

                      <label>Remark (optional)</label>
                      <textarea
                        rows={4}
                        placeholder="Why is the status changing?"
                        value={statusRemark}
                        onChange={(e) => setStatusRemark(e.target.value)}
                      />

                      <button className="admin-status-save" onClick={updateComplaintStatus}>
                        Update status
                      </button>
                    </>
                  ) : (
                    <p>No complaints found.</p>
                  )}
                </div>
              </div>
            </>
          )}
        </main>
      </div>
    </div>
  );
}

export default DashBoardAdmin;
