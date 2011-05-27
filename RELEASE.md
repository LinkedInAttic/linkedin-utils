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