import { useRef, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

const API = "http://localhost:3000";
const HOSTELS = ["A-Block", "B-Block", "C-Block", "D-Block", "Girls Hostel"];

function Landing() {
  const navigate = useNavigate();
  const usernameRef = useRef();
  const passwordRef = useRef();
  const [role, setRole] = useState("student");
  const [authMode, setAuthMode] = useState("login");
  const [hostel, setHostel] = useState(HOSTELS[0]);
  const [roomNo, setRoomNo] = useState("");

  useEffect(() => {
    setHostel(HOSTELS[0]);
  }, [role]);

  async function signUp() {
    const username = usernameRef.current?.value?.trim();
    const password = passwordRef.current?.value?.trim();
    if (!username || !password) {
      alert("Username and password required");
      return;
    }
    if (!hostel) {
      alert("Please select your hostel");
      return;
    }
    if (role === "student" && !roomNo.trim()) {
      alert("Please enter room number");
      return;
    }

    const url = role === "warden" ? `${API}/signup/warden` : `${API}/signup/student`;
    const body =
      role === "student"
        ? { username, password, hostel, room_no: roomNo.trim() }
        : { username, password, hostel };

    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    const data = await response.json();

    if (data.token) {
      localStorage.setItem("token", data.token);
      localStorage.setItem("role", role);
      navigate(`/dashboard/${role}`);
    } else {
      alert(data.message || "Sign up failed");
      usernameRef.current.value = "";
      passwordRef.current.value = "";
      setRoomNo("");
    }
  }

  async function login() {
    const username = usernameRef.current?.value?.trim();
    const password = passwordRef.current?.value?.trim();
    if (!username || !password) {
      alert("Username and password required");
      return;
    }

    const url = role === "warden" ? `${API}/signin/warden` : `${API}/signin/student`;
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    const data = await response.json();

    if (data.token) {
      localStorage.setItem("token", data.token);
      localStorage.setItem("role", role);
      navigate(`/dashboard/${role}`);
    } else {
      alert(data.message || "Login failed");
      usernameRef.current.value = "";
      passwordRef.current.value = "";
    }
  }

  return (
    <div className="landing-page ld-page">
      <div className="ld-card">
        <div className="ld-logo">🏠</div>
        <h1 className="ld-title">HostelDesk</h1>
        <p className="ld-subtitle">Sign in to your account</p>

        <div className="ld-field">
          <label>Email</label>
          <input
            ref={usernameRef}
            id="username"
            type="text"
            placeholder="yourname@college.edu"
          />
        </div>

        <div className="ld-field">
          <label>Password</label>
          <input
            ref={passwordRef}
            id="password"
            type="password"
            placeholder="••••••••"
          />
        </div>

        {authMode === "signup" && (
          <>
            <div className="ld-field">
              <label>Hostel</label>
              <select value={hostel} onChange={(e) => setHostel(e.target.value)}>
                {HOSTELS.map((h) => (
                  <option key={h} value={h}>
                    {h}
                  </option>
                ))}
              </select>
            </div>
            {role === "student" && (
              <div className="ld-field">
                <label>Room Number</label>
                <input
                  type="text"
                  placeholder="e.g. 101"
                  value={roomNo}
                  onChange={(e) => setRoomNo(e.target.value)}
                />
              </div>
            )}
          </>
        )}

        <div className="ld-row">
          <label className="ld-remember">
            <input type="checkbox" />
            Remember me
          </label>
          <button
            type="button"
            className="ld-link"
            onClick={() => alert("Password reset is not implemented yet")}
          >
            Forgot password?
          </button>
        </div>

        <button
          className="ld-submit"
          onClick={authMode === "login" ? login : signUp}
        >
          {authMode === "login" ? "Sign in →" : "Create account →"}
        </button>

        <p className="ld-meta">
          {authMode === "login" ? "Don't have an account? " : "Already have an account? "}
          <button
            type="button"
            className="ld-link"
            onClick={() => setAuthMode(authMode === "login" ? "signup" : "login")}
          >
            {authMode === "login" ? "Register" : "Sign in"}
          </button>
        </p>

        <p className="ld-sep">— or sign in as —</p>
        <div className="ld-role-wrap">
          <button
            className={`ld-role-btn ${role === "student" ? "active" : ""}`}
            onClick={() => setRole("student")}
          >
            Student
          </button>
          <button
            className={`ld-role-btn ${role === "warden" ? "active" : ""}`}
            onClick={() => setRole("warden")}
          >
            Admin
          </button>
        </div>
      </div>
    </div>
  );
}

export default Landing;
