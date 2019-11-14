## stresparser





#### StResParser

- StResParser -export {sourceFolder} {csvFileName}

  : sourceFolder 의 *.xml 과 *.strings 파일을 파싱하여 통합 후 csvFileName로 생성한다.

- StResParser -import {csvFileName} {targetFolder}

  : csvFileName 파일을 파싱하여, targetFolder 의 *.xml 과 *.strings 파일에 각 key에 대응 하는 vaule 값을 반영 한다.

> {csvFileName}.csv
> {key-android}, {key-ios}, {value}
>
>  
>
> *.xml (Android)
>
> 
>
> *.strings (iOS)



