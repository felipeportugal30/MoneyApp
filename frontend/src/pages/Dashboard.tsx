import { useState, useEffect } from "react";
import {
  TrendingUp,
  TrendingDown,
  Wallet,
  PiggyBank,
  ChevronLeft,
  ChevronRight,
  Loader2,
  ArrowDownLeft,
  ArrowUpRight,
} from "lucide-react";
import { format, subMonths, addMonths, isSameMonth, startOfMonth } from "date-fns";
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import { API_URL } from "@/config/api";

interface CategorySummary {
  category: string;
  total: number;
}

interface MonthlyEvolution {
  month: string;
  income: number;
  expenses: number;
}

interface RecentTransaction {
  id: string;
  amount: number;
  date: string;
  type: string;
  category: string;
  description: string;
}

interface DashboardData {
  totalBalance: number;
  monthlyIncome: number;
  monthlyExpenses: number;
  savingsRate: number;
  expensesByCategory: CategorySummary[];
  monthlyEvolution: MonthlyEvolution[];
  recentTransactions: RecentTransaction[];
}

const PIE_COLORS = [
  "#f59e0b", "#3b82f6", "#10b981", "#ef4444", "#8b5cf6",
  "#ec4899", "#06b6d4", "#f97316", "#84cc16", "#6366f1",
];

const fmt = (value: number) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value);

const Dashboard = () => {
  const [currentMonth, setCurrentMonth] = useState(() => subMonths(new Date(), 1));
  const today = new Date();
  const isCurrentMonth = isSameMonth(currentMonth, today);
  const monthLabel = isCurrentMonth
    ? "Current month"
    : isSameMonth(currentMonth, subMonths(today, 1))
    ? "Last month"
    : null;
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  const monthParam = format(currentMonth, "yyyy-MM");

  useEffect(() => {
    const fetchDashboard = async () => {
      setLoading(true);
      try {
        const res = await fetch(`${API_URL}/dashboard?month=${monthParam}`, {
          headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        });
        if (!res.ok) throw new Error();
        setData(await res.json());
      } catch {
        setData(null);
      } finally {
        setLoading(false);
      }
    };
    fetchDashboard();
  }, [monthParam]);

  const stats = data
    ? [
        { label: "Total Balance", value: fmt(data.totalBalance), icon: Wallet, danger: data.totalBalance < 0 },
        { label: "Monthly Income", value: fmt(data.monthlyIncome), icon: TrendingUp, danger: false },
        { label: "Monthly Expenses", value: fmt(data.monthlyExpenses), icon: TrendingDown, danger: true },
        {
          label: "Savings Rate",
          value: `${data.savingsRate.toFixed(1)}%`,
          icon: PiggyBank,
          danger: data.savingsRate < 0,
        },
      ]
    : [];

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Dashboard</h1>
          <p className="text-muted-foreground mt-1">Financial overview for the selected month</p>
        </div>
        <div className="flex flex-col items-end gap-1">
          <div className="flex items-center gap-2 glass-card rounded-xl px-4 py-2">
            <button
              onClick={() => setCurrentMonth((m) => subMonths(m, 1))}
              className="text-muted-foreground hover:text-foreground transition-colors p-1"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="text-sm font-medium text-foreground w-28 text-center">
              {format(currentMonth, "MMMM yyyy")}
            </span>
            <button
              onClick={() => setCurrentMonth((m) => addMonths(m, 1))}
              disabled={isCurrentMonth}
              className="text-muted-foreground hover:text-foreground transition-colors p-1 disabled:opacity-30 disabled:cursor-not-allowed"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
          {monthLabel && (
            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${
              isCurrentMonth
                ? "bg-warning/15 text-warning"
                : "bg-accent/10 text-accent"
            }`}>
              {monthLabel}
            </span>
          )}
        </div>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-24">
          <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
        </div>
      ) : data ? (
        <>
          {/* Stats */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {stats.map((stat) => (
              <div key={stat.label} className="glass-card rounded-xl p-5">
                <div className="flex items-center justify-between mb-3">
                  <span className="text-sm text-muted-foreground">{stat.label}</span>
                  <stat.icon className="w-4 h-4 text-muted-foreground" />
                </div>
                <span className={`text-2xl font-bold ${stat.danger ? "text-destructive" : "text-foreground"}`}>
                  {stat.value}
                </span>
              </div>
            ))}
          </div>

          {/* Charts */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <div className="glass-card rounded-xl p-5">
              <h2 className="text-lg font-semibold text-foreground mb-4">Expenses by Category</h2>
              {data.expensesByCategory.length > 0 ? (
                <ResponsiveContainer width="100%" height={240}>
                  <PieChart>
                    <Pie
                      data={data.expensesByCategory}
                      dataKey="total"
                      nameKey="category"
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={90}
                      paddingAngle={3}
                    >
                      {data.expensesByCategory.map((_, i) => (
                        <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(v: number) => fmt(v)} />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex items-center justify-center h-60 text-sm text-muted-foreground">
                  No expense data for this month
                </div>
              )}
            </div>

            <div className="glass-card rounded-xl p-5">
              <h2 className="text-lg font-semibold text-foreground mb-4">Monthly Evolution</h2>
              {data.monthlyEvolution.length > 0 ? (
                <ResponsiveContainer width="100%" height={240}>
                  <BarChart data={data.monthlyEvolution}>
                    <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                    <YAxis tick={{ fontSize: 11 }} />
                    <Tooltip formatter={(v: number) => fmt(v)} />
                    <Legend />
                    <Bar dataKey="income" name="Income" fill="#10b981" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="expenses" name="Expenses" fill="#ef4444" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex items-center justify-center h-60 text-sm text-muted-foreground">
                  No evolution data available
                </div>
              )}
            </div>
          </div>

          {/* Recent Transactions */}
          <div className="glass-card rounded-xl">
            <div className="p-5 border-b border-border">
              <h2 className="text-lg font-semibold text-foreground">Recent Transactions</h2>
            </div>
            {data.recentTransactions.length === 0 ? (
              <div className="py-16 text-center text-sm text-muted-foreground">
                No transactions for this month
              </div>
            ) : (
              <div className="divide-y divide-border">
                {data.recentTransactions.map((tx) => (
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
                          {tx.description || tx.category}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {tx.category} · {tx.date}
                        </p>
                      </div>
                    </div>
                    <span
                      className={`text-sm font-semibold shrink-0 ${
                        tx.type === "INCOME" ? "text-success" : "text-destructive"
                      }`}
                    >
                      {tx.type === "INCOME" ? "+" : "-"}
                      {fmt(Math.abs(tx.amount))}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      ) : (
        <div className="glass-card rounded-xl py-24 text-center text-sm text-muted-foreground">
          Could not load dashboard data
        </div>
      )}
    </div>
  );
};

export default Dashboard;
