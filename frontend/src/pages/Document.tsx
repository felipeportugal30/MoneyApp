import { useState, useCallback, useEffect } from "react";
import {
  Upload,
  FileText,
  X,
  CheckCircle,
  Loader2,
  History,
  Trash2,
  AlertCircle,
} from "lucide-react";
import { API_URL } from "@/config/api";
import { Button } from "@/components/ui/button";

interface UploadedFile {
  id: string;
  file: File;
  filename: string;
  size: string;
  hash: string;
  status: "ready" | "uploading" | "done" | "error";
}

interface HistoryFile {
  id: string;
  filename: string;
  size: string;
  uploadedAt: string;
  type: string;
}

type Tab = "upload" | "history";

const Documents = () => {
  const [activeTab, setActiveTab] = useState<Tab>("upload");
  const [loading, setLoading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const [files, setFiles] = useState<UploadedFile[]>([]);
  const [history, setHistory] = useState<HistoryFile[]>([]);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const generateHash = async (file: File): Promise<string> => {
    const arrayBuffer = await file.arrayBuffer();
    const hashBuffer = await crypto.subtle.digest("SHA-256", arrayBuffer);
    return Array.from(new Uint8Array(hashBuffer))
      .map((b) => b.toString(16).padStart(2, "0"))
      .join("");
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;

    const kb = bytes / 1024;
    if (kb < 1024) return `${kb.toFixed(1)} KB`;

    const mb = kb / 1024;
    if (mb < 1024) return `${mb.toFixed(1)} MB`;

    const gb = mb / 1024;
    return `${gb.toFixed(1)} GB`;
  };

  const handleFiles = useCallback(async (fileList: FileList) => {
    const newFiles: UploadedFile[] = await Promise.all(
      Array.from(fileList).map(async (f) => ({
        id: crypto.randomUUID(),
        file: f,
        filename: f.name,
        size: formatFileSize(f.size),
        hash: await generateHash(f),
        status: "ready" as const,
      }))
    );
    setFiles((prev) => [...newFiles, ...prev]);
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setDragOver(false);
      if (e.dataTransfer.files.length) handleFiles(e.dataTransfer.files);
    },
    [handleFiles]
  );

  const removeFile = (id: string) => setFiles((prev) => prev.filter((f) => f.id !== id));

  const uploadFiles = async () => {
    if (loading) return;
    setLoading(true);
    setFiles((prev) => prev.map((f) => ({ ...f, status: "uploading" })));
    try {
      const formData = new FormData();
      files.forEach((f) => {
        formData.append("files", f.file);
        formData.append("hashes", f.hash);
      });
      const response = await fetch(`${API_URL}/file/upload`, {
        method: "POST",
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        body: formData,
      });
      if (!response.ok) throw new Error("Upload failed");
      setFiles((prev) => prev.map((f) => ({ ...f, status: "done" })));
    } catch {
      setFiles((prev) => prev.map((f) => ({ ...f, status: "error" })));
    } finally {
      setLoading(false);
    }
  };

  const fetchHistory = async () => {
    try {
      const response = await fetch(`${API_URL}/file/me`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`
        }
      });

      if (!response.ok) throw new Error("Failed to fetch files")

      const data = await response.json();

      const formatted: HistoryFile[] = data.map((f: any) => ({
        id: f.id,
        filename: f.filename,
        size: formatFileSize(f.size),
        uploadedAt: new Date(f.createdAt).toLocaleDateString(),
        type: f.filename.split(".").pop()?.toUpperCase() || ""
      }));

      setHistory(formatted)
    } catch (error) {
      console.error(error)
    }
  }

  useEffect(() => {
    if (activeTab === "history") {
      fetchHistory();
    }
  }, [activeTab]);

  const deleteHistoryFile = async (id: string) => {
    setDeletingId(id);
    try {
      await fetch(`${API_URL}/file/delete/${id}`, { 
        method: "DELETE",
        headers: { 
          Authorization: `Bearer ${localStorage.getItem("token")}` 
        } 
      });
      setHistory((prev) => prev.filter((f) => f.id !== id));
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div>
        <h1 className="text-2xl font-bold text-foreground">Documents</h1>
        <p className="text-muted-foreground mt-1">Upload and manage your financial files</p>
      </div>

      {/* Tab header */}
      <div className="flex items-center gap-1 border-b border-border">
        <button
          onClick={() => setActiveTab("upload")}
          className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium transition-colors border-b-2 -mb-px ${
            activeTab === "upload"
              ? "border-accent text-accent"
              : "border-transparent text-muted-foreground hover:text-foreground"
          }`}
        >
          <Upload className="w-4 h-4" />
          Upload
        </button>
        <button
          onClick={() => setActiveTab("history")}
          className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium transition-colors border-b-2 -mb-px ${
            activeTab === "history"
              ? "border-accent text-accent"
              : "border-transparent text-muted-foreground hover:text-foreground"
          }`}
        >
          <History className="w-4 h-4" />
          History
          {history.length > 0 && (
            <span className="ml-1 px-1.5 py-0.5 text-xs rounded-full bg-secondary text-muted-foreground">
              {history.length}
            </span>
          )}
        </button>
      </div>

      {/* ── Upload tab ── */}
      {activeTab === "upload" && (
        <div className="space-y-6">
          {/* Drop zone */}
          <div
            onDragEnter={(e) => { e.preventDefault(); setDragOver(true); }}
            onDragOver={(e) => { e.preventDefault()}}
            onDragLeave={(e) => {
              e.preventDefault();
              if (!e.currentTarget.contains(e.relatedTarget as Node)) {
                setDragOver(false);
              }
            }}
            onDrop={handleDrop}
            className={`glass-card rounded-xl border-2 border-dashed p-12 text-center transition-colors duration-200 ${
              dragOver
                ? "!bg-slate-200"
                : "border-border"
            }`}
          >
            <div className="flex flex-col items-center gap-4">
              <div className="w-14 h-14 rounded-full bg-secondary flex items-center justify-center">
                <Upload className="w-6 h-6 text-muted-foreground" />
              </div>
              <div>
                <p className="text-foreground font-medium">
                  Drag & drop files here, or{" "}
                  <label className="text-accent cursor-pointer hover:underline">
                    browse
                    <input
                      type="file"
                      multiple
                      className="hidden"
                      onChange={(e) => e.target.files && handleFiles(e.target.files)}
                    />
                  </label>
                </p>
                <p className="text-sm text-muted-foreground mt-1">
                  PDF, DOCX, XLSX, CSV — up to 50 MB each
                </p>
              </div>
            </div>
          </div>

          {/* File list */}
          {files.length > 0 && (
            <div className="glass-card rounded-xl">
              <div className="p-5 border-b border-border">
                <h2 className="text-lg font-semibold text-foreground">
                  Files ({files.length})
                </h2>
              </div>
              <div className="divide-y divide-border">
                {files.map((file) => (
                  <div
                    key={file.id}
                    className="flex items-center justify-between p-4 hover:bg-muted/50 transition-colors"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center shrink-0">
                        <FileText className="w-4 h-4 text-muted-foreground" />
                      </div>
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-foreground truncate">{file.filename}</p>
                        <p className="text-xs text-muted-foreground">{file.size}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      {file.status === "uploading" && (
                        <Loader2 className="w-4 h-4 text-accent animate-spin" />
                      )}
                      {file.status === "done" && (
                        <CheckCircle className="w-4 h-4 text-green-500" />
                      )}
                      {file.status === "error" && (
                        <AlertCircle className="w-4 h-4 text-destructive" />
                      )}
                      <button
                        onClick={() => removeFile(file.id)}
                        className="text-muted-foreground hover:text-foreground transition-colors p-1"
                      >
                        <X className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
              <div className="flex justify-center p-5 border-t border-border">
                <Button
                  className="w-40"
                  onClick={uploadFiles}
                  disabled={loading || files.every((f) => f.status === "done")}
                >
                  {loading ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Sending...
                    </>
                  ) : (
                    "Send files"
                  )}
                </Button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* ── History tab ── */}
      {activeTab === "history" && (
        <div className="glass-card rounded-xl">
          {history.length === 0 ? (
            <div className="flex flex-col items-center gap-3 py-16 text-center">
              <div className="w-12 h-12 rounded-full bg-secondary flex items-center justify-center">
                <History className="w-5 h-5 text-muted-foreground" />
              </div>
              <p className="text-sm font-medium text-foreground">No files uploaded yet</p>
              <p className="text-xs text-muted-foreground">
                Go to the Upload tab to send your first file.
              </p>
            </div>
          ) : (
            <>
              <div className="p-5 border-b border-border">
                <h2 className="text-lg font-semibold text-foreground">
                  Upload history ({history.length})
                </h2>
              </div>
              <div className="divide-y divide-border">
                {history.map((file) => (
                  <div
                    key={file.id}
                    className="flex items-center justify-between p-4 hover:bg-muted/50 transition-colors"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center shrink-0">
                        <FileText className="w-4 h-4 text-muted-foreground" />
                      </div>
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-foreground truncate">{file.filename}</p>
                        <p className="text-xs text-muted-foreground">
                          {file.size} · Uploaded on {file.uploadedAt}
                        </p>
                      </div>
                    </div>
                    <button
                      onClick={() => deleteHistoryFile(file.id)}
                      disabled={deletingId === file.id}
                      className="p-2 rounded-lg text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors disabled:opacity-50"
                      title="Delete file"
                    >
                      {deletingId === file.id ? (
                        <Loader2 className="w-4 h-4 animate-spin" />
                      ) : (
                        <Trash2 className="w-4 h-4" />
                      )}
                    </button>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default Documents;