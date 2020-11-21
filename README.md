# DLP Scanner

A naive Sensitive Data scanner that provides the following functionality:
- Scan any given string (up to a configurable size)
- Scan any given file, which is locally available to the executable.

The following sensitive data types are supported:
- Social Securiry Number
- IBAN number

Scanning is done while taking contextual keywords into account in order to reduce false-positives, this is reflected in the response by the "contextRank" field which signifies
the probability of the match with regards to the surrounding context in the input.

---

### API
- This service answers on two endpoints:
  - `POST api/v1/scan/text` expects a request json structured like:
    ```
    {
      "text": "...."
    }
    ```
  - `POST api/v1/scan/file` expects a request json structured like:
    ```
    {
      "filePath": "...."
    }
    ```
    - When Passing a file via `filePath` the absolute path must point to a file that's readable by the executable 

- The response is structured as:
    ```
    [
        {
            "type": "..",
            "count": 0,
            "contextRank": 0,
        },
        ....
    ]
    ```

- Each object in the response shows one kind of sensitive data matched in the input (if any) and the count of matches found. In addition the context rank (explained above) is returned as well.
- REST API documentation is also available via the service's Swagger endpoint available at: `http://localhost:8080/swagger-ui.html`

---

### Running Examples

##### Running Locally
` mvn spring-boot:run`

##### Running Docker
- A pre-built container is also available at docker hub under `feldan/dlpengine:0.9`
- `docker run -d --rm --name dlpengine -p8080:8080 feldan/dlpengine:0.9` (or any other image/tag combo)
- You can also add a `volume` to easily support scanning files: 
`docker run -d --rm --name dlpengine -v /local_path/to/some/dir/with/files:/tmp/workdir -p 8080:8080 feldan/dlpengine:0.9`

##### Using httpie:
- `http POST http://localhost:8080/api/v1/scan text="social security 123-45-6789"`
- `http POST http://localhost:8080/api/v1/scan filePath="/path/to/my/file`
##### Using curl:
- `curl -XPOST localhost:8080/api/v1/scan -H "Content-Type: application/json" -d '{"text":"social security 123-45-6789"}'`
- `curl -XPOST localhost:8080/api/v1/scan -H "Content-Type: application/json" -d '{"filePath":"/path/to/my/file"}'`

---

#### Building
- Clone this repo
- Run `mvn clean install` (also runs tests)

#### Running Tests
- Run `mvn test`

#### Building a Docker Container
- `mvn clean install`
- `docker build -t org.danf/dlpengine:0.9 .` (or any other image/tag combo)
