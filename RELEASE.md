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