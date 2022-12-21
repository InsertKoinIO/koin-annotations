# Change Log

Badges: `[UPDATED]`, `[FIXED]`, `[NEW]`, `[DEPRECATED]`, `[REMOVED]`,  `[BREAKING]`


## [1.1.x]()

- `[UPDATED]` - Kotlin `1.7.21`
- `[UPDATED]` - Google KSP `1.7.21-1.0.8`
- `[FIXED]` - Added macosArm64 Target
- `[REMOVED]` - JS support, as array is not supported yet in annotations
- `[UPDATED]` - Multiple Round Processing PR #62
- `[FIXED]` - Import Module package generation is now fixed
- `[NEW]` - Detect injection of List<A>, to generate `getAll<A>` 
- `[NEW]` - Detect injection of Lazy<A>, to generate `inject<A>` 
- `[FIXED]` - Module visibility is now fixed from PR #63

## [1.0.x]()

### [1.0.3]()

- `[UPDATED]` - Koin 3.2.2

### [1.0.2]()

- `[UPDATED]` - KSP `1.6.21-1.0.6`
- `[MERGED]` - Gradle KSP Config for Android #39
- `[FIXED]` - KMM compatible File generation #51, #41, #8, #20
- `[FIXED]` - File Path generation #42, #40


### [1.0.1]()

- `[FIXED]` - Fixed ViewModel code Generation
- `[FIXED]` - Module Header generation 


## [1.0.0]()

- `[FIXED]` - Allow same name of Module, but different package - #14 #16
- `[FIXED]` - Allow collocated module independant of @ComponentScan annotation
- `[ADDED]` - @ComponentScan annotation scan check (avoid double declaration)


## [1.0.0-beta-2]()

- `[FIXED]` - Generating right package type with `bind` - #7
- `[FIXED]` - Remove useless dependency with `test-junit` - #6
- `[FIXED]` - Generate right StringQualifier import - #5


## [1.0.0-beta-1]()

- first public version 


