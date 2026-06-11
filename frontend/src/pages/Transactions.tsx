import { useState, useEffect } from "react";
import { Loader2, Pencil, Trash2, Filter, X, ArrowDownLeft, ArrowUpRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { API_URL } from "@/config/api";
import { toast } from "sonner";

type TransactionType = "EXPENSE" | "INCOME";
type ExpenseCategory =
  | "ALIMENTATION"
  | "TRANSPORT"
  | "HEALTH"
  | "EDUCATION"
  | "LEISURE"
  | "HOME"
  | "SHOPPING"
  | "PIX"
  | "INCOME"
  | "OTHERS";

interface Transaction {
  id: string;
  amount: number;
  date: string;
  type: TransactionType;
  category: ExpenseCategory;
  description: string;
}

const CATEGORIES: ExpenseCategory[] = [
  "ALIMENTATION",
  "TRANSPORT",
  "HEALTH",
  "EDUCATION",
  "LEISURE",
  "HOME",
  "SHOPPING",
  "PIX",
  "INCOME",
  "OTHERS",
];

const CATEGORY_LABELS: Record<ExpenseCategory, string> = {
  ALIMENTATION: "Alimentation",
  TRANSPORT: "Transport",
  HEALTH: "Health",
  EDUCATION: "Education",
  LEISURE: "Leisure",
  HOME: "Home",
  SHOPPING: "Shopping",
  PIX: "PIX",
  INCOME: "Income",
  OTHERS: "Others",
};

const ALL = "__all__";

const fmt = (value: number) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value);

const Transactions = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [showFilters, setShowFilters] = useState(false);

  const [filterCategory, setFilterCategory] = useState(ALL);
  const [filterType, setFilterType] = useState(ALL);
  const [filterStartDate, setFilterStartDate] = useState("");
  const [filterEndDate, setFilterEndDate] = useState("");

  const [editTarget, setEditTarget] = useState<Transaction | null>(null);
  const [editCategory, setEditCategory] = useState<ExpenseCategory | "">("");
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const hasFilters =
    filterCategory !== ALL ||
    filterType !== ALL ||
    !!filterStartDate ||
    !!filterEndDate;

  const fetchTransactions = async (
    category = filterCategory,
    type = filterType,
    startDate = filterStartDate,
    endDate = filterEndDate
  ) => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (category !== ALL) params.set("category", category);
      if (type !== ALL) params.set("type", type);
      if (startDate) params.set("startDate", startDate);
      if (endDate) params.set("endDate", endDate);

      const res = await fetch(`${API_URL}/transactions?${params.toString()}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
      if (!res.ok) throw new Error();
      setTransactions(await res.json());
    } catch {
      toast.error("Failed to load transactions");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  const handleApplyFilters = () => {
    fetchTransactions(filterCategory, filterType, filterStartDate, filterEndDate);
    setShowFilters(false);
  };

  const handleClearFilters = () => {
    setFilterCategory(ALL);
    setFilterType(ALL);
    setFilterStartDate("");
    setFilterEndDate("");
    fetchTransactions(ALL, ALL, "", "");
  };

  const handleEdit = (tx: Transaction) => {
    setEditTarget(tx);
    setEditCategory(tx.category);
  };

  const handleSaveEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editTarget) return;
    setSaving(true);
    try {
      const res = await fetch(`${API_URL}/transactions/${editTarget.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({ category: editCategory }),
      });
      if (!res.ok) throw new Error();
      toast.success("Transaction updated");
      setEditTarget(null);
      fetchTransactions();
    } catch {
      toast.error("Failed to update transaction");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    setDeletingId(id);
    try {
      const res = await fetch(`${API_URL}/transactions/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
      if (!res.ok) throw new Error();
      setTransactions((prev) => prev.filter((t) => t.id !== id));
      toast.success("Transaction deleted");
    } catch {
      toast.error("Failed to delete transaction");
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Transactions</h1>
          <p className="text-muted-foreground mt-1">View and manage your transactions</p>
        </div>
        <div className="flex items-center gap-2">
          {hasFilters && (
            <Button variant="outline" size="sm" onClick={handleClearFilters}>
              <X className="w-4 h-4 mr-1" />
              Clear
            </Button>
          )}
          <Button variant="outline" onClick={() => setShowFilters((v) => !v)}>
            <Filter className="w-4 h-4 mr-2" />
            Filters
          </Button>
        </div>
      </div>

      {showFilters && (
        <div className="glass-card rounded-xl p-5 space-y-4">
          <h3 className="text-sm font-semibold text-foreground">Filter transactions</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="space-y-2">
              <Label>Category</Label>
              <Select value={filterCategory} onValueChange={setFilterCategory}>
                <SelectTrigger>
                  <SelectValue placeholder="All categories" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL}>All categories</SelectItem>
                  {CATEGORIES.map((c) => (
                    <SelectItem key={c} value={c}>
                      {CATEGORY_LABELS[c]}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Type</Label>
              <Select value={filterType} onValueChange={setFilterType}>
                <SelectTrigger>
                  <SelectValue placeholder="All types" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL}>All types</SelectItem>
                  <SelectItem value="INCOME">Income</SelectItem>
                  <SelectItem value="EXPENSE">Expense</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Start date</Label>
              <Input
                type="date"
                value={filterStartDate}
                onChange={(e) => setFilterStartDate(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label>End date</Label>
              <Input
                type="date"
                value={filterEndDate}
                onChange={(e) => setFilterEndDate(e.target.value)}
              />
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <Button variant="outline" size="sm" onClick={() => setShowFilters(false)}>
              Cancel
            </Button>
            <Button size="sm" onClick={handleApplyFilters}>
              Apply filters
            </Button>
          </div>
        </div>
      )}

      <div className="glass-card rounded-xl">
        {loading ? (
          <div className="flex items-center justify-center py-24">
            <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
          </div>
        ) : transactions.length === 0 ? (
          <div className="flex flex-col items-center gap-3 py-16 text-center">
            <p className="text-sm font-medium text-foreground">No transactions found</p>
            <p className="text-xs text-muted-foreground">
              {hasFilters
                ? "Try adjusting your filters."
                : "Upload a bank statement in Documents to get started."}
            </p>
          </div>
        ) : (
          <>
            <div className="p-5 border-b border-border">
              <h2 className="text-lg font-semibold text-foreground">
                Transactions ({transactions.length})
              </h2>
            </div>
            <div className="divide-y divide-border">
              {transactions.map((tx) => (
                <div
                  key={tx.id}
                  className="flex items-center justify-between p-4 hover:bg-muted/50 transition-colors"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div
                      className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 ${
                        tx.type === "INCOME" ? "bg-success/10" : "bg-destructive/10"
                      }`}
                    >
                      {tx.type === "INCOME" ? (
                        <ArrowDownLeft className="w-4 h-4 text-success" />
                      ) : (
                        <ArrowUpRight className="w-4 h-4 text-destructive" />
                      )}
                    </div>
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-foreground truncate">
                        {tx.description || CATEGORY_LABELS[tx.category]}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {CATEGORY_LABELS[tx.category]} · {tx.date}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    <span
                      className={`text-sm font-semibold ${
                        tx.type === "INCOME" ? "text-success" : "text-destructive"
                      }`}
                    >
                      {tx.type === "INCOME" ? "+" : "-"}
                      {fmt(Math.abs(tx.amount))}
                    </span>
                    <button
                      onClick={() => handleEdit(tx)}
                      className="p-1.5 rounded-lg text-muted-foreground hover:text-foreground hover:bg-secondary transition-colors"
                    >
                      <Pencil className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => handleDelete(tx.id)}
                      disabled={deletingId === tx.id}
                      className="p-1.5 rounded-lg text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors disabled:opacity-50"
                    >
                      {deletingId === tx.id ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      ) : (
                        <Trash2 className="w-3.5 h-3.5" />
                      )}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>

      <Dialog open={!!editTarget} onOpenChange={(open) => !open && setEditTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit transaction</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSaveEdit} className="space-y-4">
            {editTarget && (
              <div className="p-4 rounded-lg bg-secondary/50">
                <p className="text-sm font-medium text-foreground">
                  {editTarget.description || CATEGORY_LABELS[editTarget.category]}
                </p>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {editTarget.date} · {fmt(Math.abs(editTarget.amount))}
                </p>
              </div>
            )}
            <div className="space-y-2">
              <Label>Category</Label>
              <Select
                value={editCategory}
                onValueChange={(v) => setEditCategory(v as ExpenseCategory)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select category" />
                </SelectTrigger>
                <SelectContent>
                  {CATEGORIES.map((c) => (
                    <SelectItem key={c} value={c}>
                      {CATEGORY_LABELS[c]}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setEditTarget(null)}>
                Cancel
              </Button>
              <Button type="submit" disabled={saving || !editCategory}>
                {saving ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" /> Saving...
                  </>
                ) : (
                  "Save"
                )}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Transactions;
