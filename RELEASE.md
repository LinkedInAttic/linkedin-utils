1.8.1 (2012/09/20)
------------------
* fixed [bug #10](https://github.com/linkedin/linkedin-utils/issues/10): _FileSystem not handling symlinks properly_

1.8.0 (2012/03/31)
------------------
* implemented [ticket #6](https://github.com/linkedin/linkedin-utils/issues/6): _Using Jackson JSON (de)serializer_ (thanks for the help from Zoran @ LinkedIn)
* fixed [bug #7](https://github.com/linkedin/linkedin-utils/issues/7): _ArrayList.size field does not exist on other JVMs but sun's_

This version uses Jackson Json parser which improves speed and memory consumption when reading/writing JSON.

Note that ``prettyPrint`` returns a slightly different output than before (keys are still sorted).

1.7.2 (2012/01/27)
------------------
* fixed [bug #5](https://github.com/linkedin/linkedin-utils/issues/5): _no Authorization header should be generated in fetchContent when not present_

1.7.1 (2011/09/20)
------------------
* fixed [bug #4](https://github.com/linkedin/linkedin-utils/issues/4): _GroovyIOUtils.toFile handles groovy string differently_

1.7.0 (2011/06/12)
------------------
* added notions of depth and distance to state machine

1.6.2 (2011/05/27)
------------------
* fixed [bug #3](https://github.com/linkedin/linkedin-utils/issues/3): _IvyURLHandler is not thread safe_

1.6.1 (2011/05/23)
------------------
* fixed [bug #2](https://github.com/linkedin/linkedin-utils/issues/2): _ClassCastException when error is a String_

1.6.0 (2011/05/04)
------------------
* added more flavors of `noException` method + testing

1.5.0 (2011/05/03)
------------------
* made `safeOverwrite` more robust
* added test for `safeOverwrite`
* added `GroovyLanUtils.noException` convenient call

1.4.0 (2011/04/30)
------------------
* fixed [bug #1](https://github.com/linkedin/linkedin-utils/issues/1): _GroovyIOUtils.cat leaks memory_

  revisited several concepts dealing with the creation of temporary files 

1.3.0 (2011/01/17)
------------------
* fixed `FileSystemImpl.chmod` to handle directories properly
* added `FileSystem.safeOverwrite` and use it in the implementation
* added `GroovyIOUtils.fetchContent` which handles basic authentication properly

1.2.1 (2010/12/20)
------------------
* use of `gradle-plugins 1.5.0` in order to support `gradle 0.9` (no version change as the code did not change)

1.2.1 (2010/12/07)
------------------
* `DataMaskingInputStream` handles file of format `key=xxx value=yyy` in addition to `name=xxx value=yyyy`

1.0.0 (2010/11/05)
------------------
* First release