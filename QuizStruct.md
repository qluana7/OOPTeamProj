### 퀴즈 저장 Json 규격

퀴즈 데이터의 저장은 다음 규격을 사용한다.

이때, 각 정의에 대하여는 다음과 같다.
```
? = nullable

string = String
number = int or long
// Choice = 객관식, Short = 주관식, Map = 지도 위치 찍기
problem_type = { Choice, Short, Maps }
url = String (file path or url)
```

```json
{
    "subject": string,
    "count": number,
    "set": [
        {
            "type": problem_type,
            "content": string,
            "image": url[]?,
            "choice": {
                "count": number,
                "content": string[]
            }?,
            "answer": string
        }, ...
    ]
}
```