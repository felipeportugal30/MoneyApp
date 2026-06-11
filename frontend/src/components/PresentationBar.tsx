
const PresentationBar = () => {
  return (
    <div className="hidden lg:flex lg:w-1/2 bg-primary flex-col items-center justify-center p-12 gap-8">
      <div className="flex items-center gap-4">
        <img src="/logo-principal.svg" alt="MoneyApp" className="w-20 h-20" />
        <span className="text-5xl font-bold text-primary-foreground font-mono">MoneyApp</span>
      </div>

      <h1 className="text-5xl font-bold leading-tight text-primary-foreground text-left">
        Manage <br />
        your future <br />
        with confidence.
      </h1>
    </div>
  )
}

export default PresentationBar
