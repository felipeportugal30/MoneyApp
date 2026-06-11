import { useState, useEffect } from "react";
import {
  Plus,
  Pencil,
  Trash2,
  Loader2,
  Wallet,
  CreditCard,
  PiggyBank,
  TrendingUp,
} from "lucide-react";
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

type AccountType = "CHECKING" | "SAVINGS" | "CREDIT_CARD" | "INVESTMENT";

interface Account {
  id: string;
  bankName: string;
  accountType: AccountType;
  balance: number;
  currency: string;
  active: boolean;
  createdAt: string;
}

const TYPE_LABELS: Record<AccountType, string> = {
  CHECKING: "Checking",
  SAVINGS: "Savings",
  CREDIT_CARD: "Credit Card",
  INVESTMENT: "Investment",
};

const TYPE_ICONS: Record<AccountType, React.ElementType> = {
  CHECKING: Wallet,
  SAVINGS: PiggyBank,
  CREDIT_CARD: CreditCard,
  INVESTMENT: TrendingUp,
};

const fmt = (value: number, currency: string) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency }).format(value);

const emptyForm = { bankName: "", accountType: "", currency: "BRL", balance: "" };

const Accounts = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<Account | null>(null);
  const [form, setForm] = useState({ ...emptyForm });
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const fetchAccounts = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/account`, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
      if (!res.ok) throw new Error();
      setAccounts(await res.json());
    } catch {
      toast.error("Failed to load accounts");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const openCreate = () => {
    setEditTarget(null);
    setForm({ ...emptyForm });
    setDialogOpen(true);
  };

  const openEdit = (account: Account) => {
    setEditTarget(account);
    setForm({
      bankName: account.bankName,
      accountType: account.accountType,
      currency: account.currency,
      balance: String(account.balance),
    });
    setDialogOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const body = {
        bankName: form.bankName,
        accountType: form.accountType,
        currency: form.currency.toUpperCase(),
        balance: parseFloat(form.balance) || 0,
      };
      const url = editTarget ? `${API_URL}/account/${editTarget.id}` : `${API_URL}/account`;
      const res = await fetch(url, {
        method: editTarget ? "PUT" : "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(body),
      });
      if (!res.ok) throw new Error();
      toast.success(editTarget ? "Account updated" : "Account created");
      setDialogOpen(false);
      fetchAccounts();
    } catch {
      toast.error("Failed to save account");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    setDeletingId(id);
    try {
      const res = await fetch(`${API_URL}/account/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
      if (!res.ok) throw new Error();
      setAccounts((prev) => prev.filter((a) => a.id !== id));
      toast.success("Account deleted");
    } catch {
      toast.error("Failed to delete account");
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Accounts</h1>
          <p className="text-muted-foreground mt-1">Manage your financial accounts</p>
        </div>
        <Button onClick={openCreate}>
          <Plus className="w-4 h-4 mr-2" />
          New account
        </Button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-24">
          <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
        </div>
      ) : accounts.length === 0 ? (
        <div className="glass-card rounded-xl flex flex-col items-center gap-3 py-16 text-center">
          <div className="w-12 h-12 rounded-full bg-secondary flex items-center justify-center">
            <Wallet className="w-5 h-5 text-muted-foreground" />
          </div>
          <p className="text-sm font-medium text-foreground">No accounts yet</p>
          <p className="text-xs text-muted-foreground">Click "New account" to add your first one.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {accounts.map((account) => {
            const Icon = TYPE_ICONS[account.accountType] ?? Wallet;
            return (
              <div key={account.id} className="glass-card rounded-xl p-5 space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-lg bg-secondary flex items-center justify-center">
                      <Icon className="w-5 h-5 text-muted-foreground" />
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-foreground">{account.bankName}</p>
                      <p className="text-xs text-muted-foreground">
                        {TYPE_LABELS[account.accountType] ?? account.accountType}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => openEdit(account)}
                      className="p-1.5 rounded-lg text-muted-foreground hover:text-foreground hover:bg-secondary transition-colors"
                    >
                      <Pencil className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => handleDelete(account.id)}
                      disabled={deletingId === account.id}
                      className="p-1.5 rounded-lg text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors disabled:opacity-50"
                    >
                      {deletingId === account.id ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      ) : (
                        <Trash2 className="w-3.5 h-3.5" />
                      )}
                    </button>
                  </div>
                </div>
                <div className="pt-2 border-t border-border">
                  <p className="text-2xl font-bold text-foreground">
                    {fmt(account.balance, account.currency)}
                  </p>
                  <p className="text-xs text-muted-foreground mt-0.5">{account.currency}</p>
                </div>
              </div>
            );
          })}
        </div>
      )}

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editTarget ? "Edit account" : "New account"}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSave} className="space-y-4">
            <div className="space-y-2">
              <Label>Bank name</Label>
              <Input
                value={form.bankName}
                onChange={(e) => setForm((f) => ({ ...f, bankName: e.target.value }))}
                placeholder="e.g. Nubank"
                required
              />
            </div>
            <div className="space-y-2">
              <Label>Account type</Label>
              <Select
                value={form.accountType}
                onValueChange={(v) => setForm((f) => ({ ...f, accountType: v }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a type" />
                </SelectTrigger>
                <SelectContent>
                  {(Object.entries(TYPE_LABELS) as [AccountType, string][]).map(([value, label]) => (
                    <SelectItem key={value} value={value}>
                      {label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>Currency</Label>
                <Input
                  value={form.currency}
                  onChange={(e) =>
                    setForm((f) => ({ ...f, currency: e.target.value.toUpperCase() }))
                  }
                  placeholder="BRL"
                  maxLength={3}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label>Balance</Label>
                <Input
                  type="number"
                  step="0.01"
                  value={form.balance}
                  onChange={(e) => setForm((f) => ({ ...f, balance: e.target.value }))}
                  placeholder="0.00"
                />
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={saving || !form.accountType || !form.bankName}>
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

export default Accounts;
