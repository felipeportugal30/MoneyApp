import { useState } from "react";
import {
  User,
  Lock,
  ChevronDown,
  ChevronUp,
  Save,
  Trash2,
  Loader2,
  AlertTriangle,
  Eye,
  EyeOff,
  LogOut,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useNavigate } from "react-router-dom";
import { API_URL } from "@/config/api";
import type { UserData } from "@/components/User";

type Section = "profile" | "password" | "danger" | null;

function SectionToggle({
  open,
  title,
  subtitle,
  icon: Icon,
  onClick,
}: {
  open: boolean;
  title: string;
  subtitle: string;
  icon: React.ElementType;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className="w-full flex items-center justify-between p-5 hover:bg-muted/50 transition-colors"
    >
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center shrink-0">
          <Icon className="w-4 h-4 text-muted-foreground" />
        </div>
        <div className="text-left">
          <p className="text-sm font-semibold text-foreground">{title}</p>
          <p className="text-xs text-muted-foreground">{subtitle}</p>
        </div>
      </div>
      {open ? (
        <ChevronUp className="w-4 h-4 text-muted-foreground" />
      ) : (
        <ChevronDown className="w-4 h-4 text-muted-foreground" />
      )}
    </button>
  );
}

const Settings = () => {
  const navigate = useNavigate();
  const [openSection, setOpenSection] = useState<Section>(null);

  // Profile form
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [savingProfile, setSavingProfile] = useState(false);
  const [profileSaved, setProfileSaved] = useState(false);

  // Password form
  const [currentPw, setCurrentPw] = useState("");
  const [newPw, setNewPw] = useState("");
  const [confirmPw, setConfirmPw] = useState("");
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [savingPw, setSavingPw] = useState(false);
  const [pwError, setPwError] = useState<string | null>(null);
  const [pwSaved, setPwSaved] = useState(false);

  // Danger zone
  const [deleteConfirm, setDeleteConfirm] = useState("");
  const [deleting, setDeleting] = useState(false);

  const userString = localStorage.getItem("user");
    if (!userString) {
      console.error("User not found");
      setDeleting(false);
      return;
    }
  const user: UserData = JSON.parse(userString);

  const toggle = (s: Section) => setOpenSection((prev) => (prev === s ? null : s));

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();

    setSavingProfile(true);
    setProfileSaved(false);

    try {
      const response = await fetch(`${API_URL}/users/update`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({ email, name }),
      });

      if (!response.ok) {
        throw new Error("Failed saving informations");
      }

      setProfileSaved(true);
      setTimeout(() => setProfileSaved(false), 3000);

    } catch (error) {
      console.error(error);
    } finally {
      setSavingProfile(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setPwError(null);
    if (newPw !== confirmPw) { setPwError("Passwords do not match."); return; }
    if (newPw.length < 8) { setPwError("Password must be at least 8 characters."); return; }
    setSavingPw(true);
    try {
      await new Promise((r) => setTimeout(r, 700)); // ajustar com a API real
      setPwSaved(true);
      setCurrentPw(""); setNewPw(""); setConfirmPw("");
      setTimeout(() => setPwSaved(false), 3000);
    } finally {
      setSavingPw(false);
    }
  };

  const handleDeleteAccount = async () => {
    if (deleteConfirm !== "delete my account") return;
    setDeleting(true);
    
    try {
      const response = await fetch(`${API_URL}/users/delete/${user.id}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed deleting user");
      }

      navigate("/login");

    } catch (error) {
      console.error(error);
    } finally {
      setDeleting(false);
    }
  };

  const handleSignOut = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <div className="space-y-6 max-w-2xl">
      {/* Page header */}
      <div>
        <h1 className="text-2xl font-bold text-foreground">Settings</h1>
        <p className="text-muted-foreground mt-1">Manage your account and preferences</p>
      </div>

      {/* Account card */}
      <div className="glass-card rounded-xl border border-border overflow-hidden divide-y divide-border">

        {/* Profile info */}
        <div>
          <SectionToggle
            open={openSection === "profile"}
            title="Profile information"
            subtitle="Update your name and email address"
            icon={User}
            onClick={() => toggle("profile")}
          />
          {openSection === "profile" && (
            <form onSubmit={handleSaveProfile} className="px-5 pb-5 space-y-4">
              {/* Read-only summary */}
              <div className="p-4 rounded-lg bg-secondary/50 flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-accent/20 flex items-center justify-center shrink-0">
                  <span className="text-sm font-bold text-accent">
                    {name.split(" ").map((n) => n[0]).join("").slice(0, 2).toUpperCase()}
                  </span>
                </div>
                <div>
                  <p className="text-sm font-semibold text-foreground">{name}</p>
                  <p className="text-xs text-muted-foreground">Member since {new Date(user.createdAt).toDateString()}</p>
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="name">Full name</Label>
                <Input
                  id="name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Your full name"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="email">Email address</Label>
                <Input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@example.com"
                  required
                />
              </div>
              <div className="flex items-center gap-3">
                <Button type="submit" disabled={savingProfile}>
                  {savingProfile ? (
                    <><Loader2 className="w-4 h-4 mr-2 animate-spin" /> Saving...</>
                  ) : (
                    <><Save className="w-4 h-4 mr-2" /> Save changes</>
                  )}
                </Button>
                {profileSaved && (
                  <p className="text-sm text-green-500 font-medium">Changes saved!</p>
                )}
              </div>
            </form>
          )}
        </div>

        {/* Change password */}
        <div>
          <SectionToggle
            open={openSection === "password"}
            title="Change password"
            subtitle="Update your current password"
            icon={Lock}
            onClick={() => toggle("password")}
          />
          {openSection === "password" && (
            <form onSubmit={handleChangePassword} className="px-5 pb-5 space-y-4">
              <div className="space-y-2">
                <Label htmlFor="current-pw">Current password</Label>
                <div className="relative">
                  <Input
                    id="current-pw"
                    type={showCurrent ? "text" : "password"}
                    value={currentPw}
                    onChange={(e) => setCurrentPw(e.target.value)}
                    placeholder="••••••••"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowCurrent(!showCurrent)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  >
                    {showCurrent ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="new-pw">New password</Label>
                <div className="relative">
                  <Input
                    id="new-pw"
                    type={showNew ? "text" : "password"}
                    value={newPw}
                    onChange={(e) => setNewPw(e.target.value)}
                    placeholder="Min. 8 characters"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowNew(!showNew)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  >
                    {showNew ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="confirm-pw">Confirm new password</Label>
                <Input
                  id="confirm-pw"
                  type="password"
                  value={confirmPw}
                  onChange={(e) => setConfirmPw(e.target.value)}
                  placeholder="••••••••"
                  required
                />
              </div>
              {pwError && (
                <p className="text-sm text-destructive">{pwError}</p>
              )}
              <div className="flex items-center gap-3">
                <Button type="submit" disabled={savingPw}>
                  {savingPw ? (
                    <><Loader2 className="w-4 h-4 mr-2 animate-spin" /> Saving...</>
                  ) : (
                    <><Save className="w-4 h-4 mr-2" /> Update password</>
                  )}
                </Button>
                {pwSaved && (
                  <p className="text-sm text-green-500 font-medium">Password updated!</p>
                )}
              </div>
            </form>
          )}
        </div>

        {/* Danger zone */}
        <div>
          <SectionToggle
            open={openSection === "danger"}
            title="Delete account"
            subtitle="Permanently remove your account and all data"
            icon={Trash2}
            onClick={() => toggle("danger")}
          />
          {openSection === "danger" && (
            <div className="px-5 pb-5 space-y-4">
              <div className="flex items-start gap-3 p-4 rounded-lg bg-destructive/10 border border-destructive/20">
                <AlertTriangle className="w-4 h-4 text-destructive mt-0.5 shrink-0" />
                <p className="text-sm text-destructive">
                  This action is <strong>irreversible</strong>. All your files, data and history will be permanently deleted.
                </p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="delete-confirm">
                  Type <span className="font-mono text-destructive">delete my account</span> to confirm
                </Label>
                <Input
                  id="delete-confirm"
                  value={deleteConfirm}
                  onChange={(e) => setDeleteConfirm(e.target.value)}
                  placeholder="delete my account"
                />
              </div>
              <Button
                variant="destructive"
                disabled={deleteConfirm !== "delete my account" || deleting}
                onClick={handleDeleteAccount}
              >
                {deleting ? (
                  <><Loader2 className="w-4 h-4 mr-2 animate-spin" /> Deleting...</>
                ) : (
                  <><Trash2 className="w-4 h-4 mr-2" /> Delete my account</>
                )}
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* Sign out */}
      <div className="glass-card rounded-xl border border-border">
        <button
          onClick={handleSignOut}
          className="w-full flex items-center gap-3 p-5 text-sm font-medium text-muted-foreground hover:text-destructive hover:bg-destructive/5 transition-colors rounded-xl"
        >
          <LogOut className="w-4 h-4" />
          Sign out
        </button>
      </div>
    </div>
  );
};

export default Settings;