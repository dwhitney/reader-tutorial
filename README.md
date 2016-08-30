# Reader Monad

This shows how I use the reader monad for DI. In the `same-app-with-constructor-based-id` branch you can see the same app written with constructor based DI for comparison. One more note, you must create a file called `src/main/resources/secret.conf` with AWS credentials for a user that has full access to SQS and DynamoDB that looks like the following:

```
aws{

  credentials{
    access-key-id = ""
    secret-access-key = ""
  }

}
```
