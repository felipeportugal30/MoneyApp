import { useState, useCallback } from "react";
import { Upload, FileText, X, CheckCircle, Loader2 } from "lucide-react";

interface UploadedFile {
  id: string;
  name: string;
  size: string;
  status: "uploading" | "done" | "error";
}

const Documents = () => {
  const [dragOver, setDragOver] = useState(false);
  const [files, setFiles] = useState<UploadedFile[]>([
    { id: "1", name: "Contract_2024.pdf", size: "1.2 MB", status: "done" },
    { id: "2", name: "Design_Specs.docx", size: "3.4 MB", status: "done" },
  ]);

  const handleFiles = useCallback((fileList: FileList) => {
    const newFiles: UploadedFile[] = Array.from(fileList).map((f) => ({
      id: crypto.randomUUID(),
      name: f.name,
      size: `${(f.size / 1024 / 1024).toFixed(1)} MB`,
      status: "uploading" as const,
    }));
    setFiles((prev) => [...newFiles, ...prev]);

    // Simulate upload completion
    newFiles.forEach((file) => {
      setTimeout(() => {
        setFiles((prev) =>
          prev.map((f) => (f.id === file.id ? { ...f, status: "done" } : f))
        );
      }, 1500 + Math.random() * 1500);
    });
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setDragOver(false);
      if (e.dataTransfer.files.length) handleFiles(e.dataTransfer.files);
    },
    [handleFiles]
  );

  const removeFile = (id: string) => {
    setFiles((prev) => prev.filter((f) => f.id !== id));
  };

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Documents</h1>
        <p className="text-muted-foreground mt-1">Upload and manage your files</p>
      </div>

      {/* Drop Zone */}
      <div
        onDragOver={(e) => {
          e.preventDefault();
          setDragOver(true);
        }}
        onDragLeave={() => setDragOver(false)}
        onDrop={handleDrop}
        className={`glass-card rounded-xl border-2 border-dashed p-12 text-center transition-colors ${
          dragOver ? "border-accent bg-accent/5" : "border-border"
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
              PDF, DOCX, XLSX, PPTX — up to 50 MB each
            </p>
          </div>
        </div>
      </div>

      {/* File List */}
      {files.length > 0 && (
        <div className="glass-card rounded-xl">
          <div className="p-5 border-b border-border">
            <h2 className="text-lg font-semibold text-foreground">
              Uploaded Files ({files.length})
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
                    <p className="text-sm font-medium text-foreground truncate">{file.name}</p>
                    <p className="text-xs text-muted-foreground">{file.size}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  {file.status === "uploading" && (
                    <Loader2 className="w-4 h-4 text-accent animate-spin" />
                  )}
                  {file.status === "done" && (
                    <CheckCircle className="w-4 h-4 text-success" />
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
        </div>
      )}
    </div>
  );
};

export default Documents;
