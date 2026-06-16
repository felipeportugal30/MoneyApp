import { useState, useRef, useEffect, useCallback } from "react";
import { Bot, Send, User, Loader2, AlertCircle, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { API_URL } from "@/config/api";

interface Message {
  id: string;
  role: "user" | "assistant" | "error";
  content: string;
  date: Date;
}

// ── Markdown renderer (shared pattern from Insights page) ──────────────────

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
    if (!bulletBuffer.length) return;
    elements.push(
      <ul key={key} className="space-y-1 pl-1 mt-1">
        {bulletBuffer.map((item, i) => (
          <li key={i} className="flex gap-2 text-sm leading-relaxed">
            <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-accent/70 shrink-0" />
            <span>{renderInline(item)}</span>
          </li>
        ))}
      </ul>
    );
    bulletBuffer = [];
  };

  const flushNumbered = (key: string) => {
    if (!numberedBuffer.length) return;
    elements.push(
      <ol key={key} className="space-y-1 pl-1 mt-1">
        {numberedBuffer.map(({ n, text }, i) => (
          <li key={i} className="flex gap-2 text-sm leading-relaxed">
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

    const headingMatch = trimmed.match(/^#{1,3}\s+(.+)/);
    if (headingMatch) {
      flushBullets(`bl-${idx}`);
      flushNumbered(`nl-${idx}`);
      elements.push(
        <p key={idx} className="text-sm font-bold text-foreground mt-2 first:mt-0">
          {renderInline(headingMatch[1])}
        </p>
      );
      return;
    }

    const boldHeading = trimmed.match(/^\*\*([^*]+)\*\*:?$/);
    if (boldHeading) {
      flushBullets(`bl-${idx}`);
      flushNumbered(`nl-${idx}`);
      elements.push(
        <p key={idx} className="text-xs font-bold text-accent uppercase tracking-wide mt-2 first:mt-0">
          {boldHeading[1].replace(/:$/, "")}
        </p>
      );
      return;
    }

    const bulletMatch = trimmed.match(/^[-*•]\s+(.+)/);
    if (bulletMatch) {
      flushNumbered(`nl-${idx}`);
      bulletBuffer.push(bulletMatch[1]);
      return;
    }

    const numberedMatch = trimmed.match(/^(\d+)\.\s+(.+)/);
    if (numberedMatch) {
      flushBullets(`bl-${idx}`);
      numberedBuffer.push({ n: numberedMatch[1], text: numberedMatch[2] });
      return;
    }

    flushBullets(`bl-${idx}`);
    flushNumbered(`nl-${idx}`);
    elements.push(
      <p key={idx} className="text-sm leading-relaxed">
        {renderInline(trimmed)}
      </p>
    );
  });

  flushBullets("bl-end");
  flushNumbered("nl-end");
  return elements;
};

// ── Component ──────────────────────────────────────────────────────────────

const WELCOME: Message = {
  id: "welcome",
  role: "assistant",
  content:
    "Olá! Sou seu assistente financeiro. Posso analisar seus gastos, comparar categorias, identificar padrões e dar conselhos práticos. Como posso ajudar?",
  date: new Date(),
};

const Chat = () => {
  const [messages, setMessages] = useState<Message[]>([WELCOME]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  // Auto-resize textarea
  useEffect(() => {
    const el = textareaRef.current;
    if (!el) return;
    el.style.height = "auto";
    el.style.height = `${Math.min(el.scrollHeight, 160)}px`;
  }, [input]);

  const handleSend = useCallback(async () => {
    const text = input.trim();
    if (!text || loading) return;

    setMessages((prev) => [
      ...prev,
      { id: crypto.randomUUID(), role: "user", content: text, date: new Date() },
    ]);
    setInput("");
    setLoading(true);

    try {
      const res = await fetch(`${API_URL}/conversation`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({ message: text }),
      });

      if (!res.ok) throw new Error();

      const data = await res.json();

      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          role: data.sucess ? "assistant" : "error",
          content: data.answer || "Não foi possível gerar uma resposta.",
          date: new Date(data.date),
        },
      ]);
    } catch {
      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          role: "error",
          content: "Falha ao conectar com o assistente. Tente novamente.",
          date: new Date(),
        },
      ]);
    } finally {
      setLoading(false);
      textareaRef.current?.focus();
    }
  }, [input, loading]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const clearChat = () => setMessages([WELCOME]);

  return (
    <div className="flex flex-col h-[calc(100vh-6rem)]">
      {/* Header */}
      <div className="mb-6 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-foreground">AI Assistant</h1>
          <p className="text-muted-foreground mt-1">
            Ask questions about your finances and get personalized advice
          </p>
        </div>
        {messages.length > 1 && (
          <Button variant="outline" size="sm" onClick={clearChat} className="shrink-0">
            <Trash2 className="w-3.5 h-3.5 mr-1.5" />
            Clear chat
          </Button>
        )}
      </div>

      {/* Chat window */}
      <div className="flex-1 glass-card rounded-xl flex flex-col overflow-hidden min-h-0">
        {/* Messages */}
        <div className="flex-1 overflow-y-auto p-5 space-y-5">
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`flex items-start gap-3 ${msg.role === "user" ? "flex-row-reverse" : ""}`}
            >
              {/* Avatar */}
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
                  msg.role === "user"
                    ? "bg-accent/20"
                    : msg.role === "error"
                    ? "bg-destructive/10"
                    : "bg-secondary"
                }`}
              >
                {msg.role === "user" ? (
                  <User className="w-4 h-4 text-accent" />
                ) : msg.role === "error" ? (
                  <AlertCircle className="w-4 h-4 text-destructive" />
                ) : (
                  <Bot className="w-4 h-4 text-muted-foreground" />
                )}
              </div>

              {/* Bubble */}
              <div
                className={`max-w-[75%] rounded-2xl px-4 py-3 text-sm ${
                  msg.role === "user"
                    ? "bg-accent text-white rounded-tr-sm"
                    : msg.role === "error"
                    ? "bg-destructive/10 text-destructive rounded-tl-sm"
                    : "bg-secondary text-foreground rounded-tl-sm"
                }`}
              >
                {msg.role === "assistant" ? (
                  <div className="space-y-1.5">{renderMarkdown(msg.content)}</div>
                ) : (
                  <p className="leading-relaxed">{msg.content}</p>
                )}
                <p
                  className={`text-xs mt-2 ${
                    msg.role === "user" ? "text-white/60 text-right" : "text-muted-foreground"
                  }`}
                >
                  {msg.date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                </p>
              </div>
            </div>
          ))}

          {/* Typing indicator */}
          {loading && (
            <div className="flex items-start gap-3">
              <div className="w-8 h-8 rounded-full bg-secondary flex items-center justify-center shrink-0">
                <Bot className="w-4 h-4 text-muted-foreground" />
              </div>
              <div className="bg-secondary rounded-2xl rounded-tl-sm px-4 py-3.5 flex items-center gap-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-muted-foreground animate-bounce [animation-delay:0ms]" />
                <span className="w-1.5 h-1.5 rounded-full bg-muted-foreground animate-bounce [animation-delay:150ms]" />
                <span className="w-1.5 h-1.5 rounded-full bg-muted-foreground animate-bounce [animation-delay:300ms]" />
              </div>
            </div>
          )}

          <div ref={bottomRef} />
        </div>

        {/* Input area */}
        <div className="border-t border-border p-4 flex items-end gap-3">
          <textarea
            ref={textareaRef}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Pergunte sobre suas finanças… (Enter para enviar, Shift+Enter para nova linha)"
            rows={1}
            className="flex-1 resize-none rounded-xl border border-input bg-background px-4 py-3 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-1 overflow-y-auto"
            style={{ lineHeight: "1.5" }}
            disabled={loading}
          />
          <button
            onClick={handleSend}
            disabled={!input.trim() || loading}
            className="w-11 h-11 rounded-xl bg-accent flex items-center justify-center text-white transition-opacity hover:opacity-90 disabled:opacity-40 shrink-0"
          >
            {loading ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Send className="w-4 h-4" />
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default Chat;
