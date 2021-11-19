## Koin KSP Compiler - TODO 🚧

Basic Definition Options for Type & Functions: (In Progress)
- Create at start ✅
- Qualifier (@Named) ✅

Class Module ✅
- Component Scan ✅
- Class modules in same packe, but different component scan ✅

- Keywords extension & Dynamic import ✅
- Android Keywords
    - @KoinViewModel ✅
    - @Fragment 🚧
    - @Worker 🚧

- Generate defaultModule if needed ✅
  
Parameter Injection (@Param) - it.getParam() ✅
- Ctor ✅
- Fun ✅

Property (@Property) - getProperty(key) ✅
- Ctor ✅
- Fun ✅

Handle Nullable Type injection
- dependency ✅
- parameter ✅
- property ✅

Scope Structure (@Scope)
- List all scopes structures and prepare for generation ✅
- @Scope on a type T -> generated scoped { T } in given scope  ✅
- except if tagged @Factory, @ViewModel or any kind of factory component ✅
- Extract as Koin module project ✅

