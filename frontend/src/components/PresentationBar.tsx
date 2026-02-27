

const PresentationBar = () => {
    return (
      <div className="hidden lg:flex lg:w-1/2 bg-primary items-center justify-center p-12">
        <div className="max-w-md text-primary-foreground">
          <div className="flex items-center gap-3 mb-8 justify-center">
            <div className="flex items-center justify-center">
                <img src="/logo-principal.svg" alt="Logo principal"/>
                <h1 className="text-4xl font-bold leading-tight ml-4 font-mono">MoneyApp</h1>
            </div>
          </div>
          <div className="flex items-center justify-center">
            <h1 className="text-4xl font-bold leading-tight mb-4">
              Manage <br />
              your future <br />
              with confidence.
            </h1>
          </div>
        </div>
      </div>
    )
}

export default PresentationBar