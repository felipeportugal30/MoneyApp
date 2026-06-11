import { useState, useEffect } from "react";
import { Lightbulb, RefreshCw, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { API_URL } from "@/config/api";
import { toast } from "sonner";

interface InsightData {
  period: string;
  insights: string;
}

// Renders inline **bold** text
const renderInline = (text: string) => {
  const parts = text.split(/(\*\*[^*]+\*\*)/g);
  return parts.map((part, i) =>
    part.startsWith("**") && part.endsWith("**") ? (
      <strong key={i} className="font-semibold text-foreground">
        {part.slice(2, -2)}
      </strong>
    ) : (
      part
    )
  );
};

const renderMarkdown = (raw: string) => {
  const lines = raw.split("\n");
  const elements: React.ReactNode[] = [];
  let bulletBuffer: string[] = [];
  let numberedBuffer: { n: string; text: string }[] = [];

  const flushBullets = (key: string) => {
    if (bulletBuffer.length === 0) return;
    elements.push(
      <ul key={key} className="space-y-1.5 pl-1">
        {bulletBuffer.map((item, i) => (
          <li key={i} className="flex gap-2 text-sm text-foreground/90 leading-relaxed">
            <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-accent shrink-0" />
            <span>{renderInline(item)}</span>
          </li>
        ))}
      </ul>
    );
    bulletBuffer = [];
  };

  const flushNumbered = (key: string) => {
    if (numberedBuffer.length === 0) return;
    elements.push(
      <ol key={key} className="space-y-1.5 pl-1">
        {numberedBuffer.map(({ n, text }, i) => (
          <li key={i} className="flex gap-2.5 text-sm text-foreground/90 leading-relaxed">
            <span className="shrink-0 font-semibold text-accent">{n}.</span>
            <span>{renderInline(text)}</span>
          </li>
        ))}
      </ol>
    );
    numberedBuffer = [];
  };

  lines.forEach((line, idx) => {
    const trimmed = line.trim();
    if (!trimmed) {
      flushBullets(`bl-${idx}`);
      flushNumbered(`nl-${idx}`);
      return;
    }

    // ## Heading or # Heading
    const headingMatch = trimmed.match(/^#{1,3}\s+(.+)/);
    if (headingMatch) {
      flushBullets(`bl-${idx}`);
      flushNumbered(`nl-${idx}`);
      elements.push(
        <h3 key={idx} className="text-base font-bold text-foreground mt-4 first:mt-0 pb-1 border-b border-border/60">
          {renderInline(headingMatch[1])}
        </h3>
      );
      return;
    }

    // **Title** or **Title:** as standalone line = section heading
    const boldHeading = trimmed.match(/^\*\*([^*]+)\*\*:?$/);
    if (boldHeading) {
      flushBullets(`bl-${idx}`);
      flushNumbered(`nl-${idx}`);
      elements.push(
        <h4 key={idx} className="text-sm font-bold text-foreground mt-3 first:mt-0 uppercase tracking-wide text-accent">
          {boldHeading[1].replace(/:$/, "")}
        </h4>
      );
      return;
    }

    // Bullet: - text | * text | • text
    const bulletMatch = trimmed.match(/^[-*•]\s+(.+)/);
    if (bulletMatch) {
      flushNumbered(`nl-${idx}`);
      bulletBuffer.push(bulletMatch[1]);
      return;
    }

    // Numbered: 1. text
    const numberedMatch = trimmed.match(/^(\d+)\.\s+(.+)/);
    if (numberedMatch) {
      flushBullets(`bl-${idx}`);
      numberedBuffer.push({ n: numberedMatch[1], text: numberedMatch[2] });
      return;
    }

    // Regular paragraph
    flushBullets(`bl-${idx}`);
    flushNumbered(`nl-${idx}`);
    elements.push(
      <p key={idx} className="text-sm text-foreground/90 leading-relaxed">
        {renderInline(trimmed)}
      </p>
    );
  });

  flushBullets("bl-end");
  flushNumbered("nl-end");

  return elements;
};

const Insights = () => {
  const [data, setData] = useState<InsightData | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchInsights = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_URL}/insights`, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
      if (!res.ok) throw new Error();
      setData(await res.json());
    } catch {
      toast.error("Failed to load insights");
      setData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInsights();
  }, []);

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold text-foreground">Financial Insights</h1>
          <p className="text-muted-foreground mt-1">AI-generated analysis of your finances</p>
        </div>
        <Button variant="outline" onClick={fetchInsights} disabled={loading}>
          <RefreshCw className={`w-4 h-4 mr-2 ${loading ? "animate-spin" : ""}`} />
          Refresh
        </Button>
      </div>

      {loading ? (
        <div className="flex flex-col items-center gap-3 py-24 text-center">
          <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
          <p className="text-sm text-muted-foreground">Analyzing your finances...</p>
        </div>
      ) : data ? (
        <div className="glass-card rounded-xl">
          <div className="p-5 border-b border-border flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-accent/10 flex items-center justify-center">
              <Lightbulb className="w-4 h-4 text-accent" />
            </div>
            <div>
              <p className="text-sm font-semibold text-foreground">Analysis</p>
              <p className="text-xs text-muted-foreground">Period: {data.period}</p>
            </div>
          </div>
          <div className="p-6 space-y-3">
            {renderMarkdown(data.insights)}
          </div>
        </div>
      ) : (
        <div className="glass-card rounded-xl flex flex-col items-center gap-3 py-16 text-center">
          <div className="w-12 h-12 rounded-full bg-secondary flex items-center justify-center">
            <Lightbulb className="w-5 h-5 text-muted-foreground" />
          </div>
          <p className="text-sm font-medium text-foreground">No insights available</p>
          <p className="text-xs text-muted-foreground">
            Add some transactions to get AI-powered financial insights.
          </p>
        </div>
      )}
    </div>
  );
};

export default Insights;
