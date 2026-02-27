import { FileText, Upload, Users, TrendingUp, Clock } from "lucide-react";

const stats = [
  { label: "Total Documents", value: "1,248", icon: FileText, change: "+12%" },
  { label: "Uploaded Today", value: "23", icon: Upload, change: "+5%" },
  { label: "Team Members", value: "8", icon: Users, change: "" },
  { label: "Storage Used", value: "4.2 GB", icon: TrendingUp, change: "67%" },
];

const recentDocs = [
  { name: "Q4 Financial Report.pdf", size: "2.4 MB", date: "2 hours ago", status: "Processed" },
  { name: "Employee Handbook.docx", size: "1.1 MB", date: "5 hours ago", status: "Processed" },
  { name: "Marketing Strategy.pptx", size: "8.7 MB", date: "Yesterday", status: "Processing" },
  { name: "Invoice #1042.pdf", size: "340 KB", date: "Yesterday", status: "Processed" },
  { name: "Product Roadmap.xlsx", size: "560 KB", date: "2 days ago", status: "Processed" },
];

const Dashboard = () => {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-foreground">Dashboard</h1>
        <p className="text-muted-foreground mt-1">Overview of your document activity</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat) => (
          <div key={stat.label} className="glass-card rounded-xl p-5">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm text-muted-foreground">{stat.label}</span>
              <stat.icon className="w-4 h-4 text-muted-foreground" />
            </div>
            <div className="flex items-end gap-2">
              <span className="text-2xl font-bold text-foreground">{stat.value}</span>
              {stat.change && (
                <span className="text-xs font-medium text-success mb-1">{stat.change}</span>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Recent Documents */}
      <div className="glass-card rounded-xl">
        <div className="p-5 border-b border-border">
          <h2 className="text-lg font-semibold text-foreground">Recent Documents</h2>
        </div>
        <div className="divide-y divide-border">
          {recentDocs.map((doc) => (
            <div key={doc.name} className="flex items-center justify-between p-5 hover:bg-muted/50 transition-colors">
              <div className="flex items-center gap-3 min-w-0">
                <div className="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center shrink-0">
                  <FileText className="w-4 h-4 text-muted-foreground" />
                </div>
                <div className="min-w-0">
                  <p className="text-sm font-medium text-foreground truncate">{doc.name}</p>
                  <p className="text-xs text-muted-foreground">{doc.size}</p>
                </div>
              </div>
              <div className="flex items-center gap-4 shrink-0">
                <div className="hidden sm:flex items-center gap-1.5 text-xs text-muted-foreground">
                  <Clock className="w-3 h-3" />
                  {doc.date}
                </div>
                <span
                  className={`text-xs font-medium px-2.5 py-1 rounded-full ${
                    doc.status === "Processed"
                      ? "bg-success/10 text-success"
                      : "bg-warning/10 text-warning"
                  }`}
                >
                  {doc.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
