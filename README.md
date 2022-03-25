```
Personia’s initial requirements were the following:
1. I would like a pure REST API to post the JSON from Chris. This JSON represents an Employee -> Supervisor relationship that looks like this:
  {
          "Pete": "Nick",
          "Barbara": "Nick",
          "Nick": "Sophie",
          "Sophie": "Jonas"
  }
In this case, Nick is a supervisor of Pete and Barbara, Sophie supervises Nick. The supervisor list is not always in order.
2. As a response to querying the endpoint, I would like to have a properly formatted JSON which reflects the employee hierarchy in a way, where the most senior employee is at the top of the JSON nested dictionary. For instance, previous input would result in:
  {
          "Jonas": {
              "Sophie": {
                  "Nick": {
                      "Pete": {},
                      "Barbara": {}
                  }
              } 
          }
  }
Sometimes Chris gives me nonsense hierarchies that contain loops or multiple roots. I would be grateful if the endpoint could handle the mistakes and tell her what went wrong. The more detailed the error messages are, the better!
 The HR Operating System
3. I would really like it if the hierarchy could be stored in a relational database (e.g. SQLite) and queried to get the supervisor and the supervisor’s supervisor of a given employee. I want to send the name of an employee to an endpoint, and receive the name of the supervisor and the name of the supervisor’s supervisor in return.
4. I would like the API to be secure so that only I can use it. Please implement some kind of authentication.


What we expect from you:
1. Write a small and simple application according to Personia’s specifications, no more, no less.
2. Provide clear and easy installation instructions (think about the reviewers!). The less steps we have to do to get this running, the better. If we can’t get your app to run, we won’t be able to review it. Docker is your friend!
3. A set of working unit/functional tests to cover all the use cases you can think of.
Ideally take our challenge in Kotlin or Java (whichever you feel most comfortable with). PHP and Ruby are also acceptable, since we use them in our legacy applications. If you prefer another language, please reach out to us first. Remember that your solution must not require us to install any stack specific tool in order to test the result of your work, so use Docker in this case.


What we (mainly) look at when checking out the solution:
1. Did you follow the instructions, i.e. does your solution work and meet Personia’s requirements? Would Personia be happy to use your solution for her work?
2. Is your solution easy to read and understand? Would we be happy to contribute to it?
3. Is your code ready for production (stable, secure, scalable)?
```


# How to run it:

1. `docker build -t personio/challenge .`
2. `docker run -d -p 4567:4567  personio/challenge`
3. Check application is running `curl localhost:4567`

# Security

* __High__: Use SSL and/or JSR protocol for sending password
* __High__: Store JWT secret into secure system or 3er party provider like [Vault](https://www.vaultproject.io/)
* __High__: Store JWT username and password in DB with SALT or 3er party provider
* __Medium__: Create endpoint in application to extend or renew automatically JWT token when expired

# Scalability

* __High__: Instead of in memory DB, use 3rd party Relational Database like [Postgresql](https://www.postgresql.org/)
* __High__: To scale hands-off first approach: Use managed DB of 3rd party provider
  like [Amazon RDS](https://aws.amazon.com/rds/postgresql/)
* __High__: Instead of using static functions use dependency injection like [Guice](https://github.com/google/guice)
* __Medium__: Load testing like [Locust](https://locust.io)
* __Low__: Token in headers instead of query parameter to avoid max length in url


# Technology decision

* __JWT__ vs __base64__:
  `JWT` and `base64` ensure that the data remain intact without modification during transport.
  * Pros: 
    * Encoding in `base64` doesn't add encryption, it means `base64` is easier to find out the original message
    * `JWT` allows [Claims](https://en.wikipedia.org/wiki/Claims-based_identity) to add identity
  * Cons:
    * `JWT` Tokens cannot be revoked, if the token gets leaked, an attacker can misuse it until the token expiry
    * `JWT` adds an extra layer of complexity

# Questions
- [ ] __JWT TOKEN in headers__
- [ ] __Persistence ORM (1 vs 2 hierarchies, A->B, B->C)__
- [ ] __Error handling (trailing coma)__
- [ ] __Status code for responses__
- [ ] __Restfull API__
- [ ] __Pyramid of testing__
- [ ] __Factory pattern vs adapted__