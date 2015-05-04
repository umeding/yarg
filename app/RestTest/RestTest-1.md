# <a name="get"></a>__GET__ `/app/rest/test/{id}`
Some GET call
### Available responses
Code | Type | Description
--- | --- | ---
__200__ | `application/json`|{"status":"OK"}
__400__ | `text/plain`|On Error

# <a name="post"></a>__POST__ `/app/rest/test/{id}`
Some test call. This is a longer description of the call. More text on another line.
### Resource template parameters
Parameter | Type | Description
--- | --- | ---
id | String | Identifier
### Request parameters
`application/json`, for example:
```json
{
"key": "SomeKey",
"value": "SomeValue"
}

```
### Available responses
Code | Type | Description
--- | --- | ---
__200__ | `application/json`|{"status":"OK"}
__400__ | `text/plain`|On Error
### Examples
1. Some bash example

   ```bash
   $ curl https://some.site.com/app/rest/test/123
   {"status":"ok"}
   ```

2. Some Java example

   ```java
   public static void main(String ... av) {
     System.out.println("laber lall");
   }
   ```


