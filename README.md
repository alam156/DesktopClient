# Desktop Client
`clone from develop-beta branch`
`Change 309'th line's output directory`

### Current path
```
signedDocument.save("/Users/bccca/Desktop/deliverables/signed-final.pdf");
```

# Hash Signer Application
This is a HashSigner application which will run in background while applying digital signature in a pdf or xml file.

# REST API documentation

### Request
`POST localhost:8089/api/show-certificates-list`
### Request Body
```
{
	"type" : "Dongle" or "Native"
}
```
### Response
```
{
	"status" : "success",
	"message": "Certificate Fetched Successfully",
	"code": "200",
	"certificateList": [
		{
			"name" : Certificate's Distinguished Name(DN),
			"alias": Certificate's Alias,
			"publicKey": Certificate's public key as Base64 string,
			"certificate": Certificate as Base64 string,
			"certificateChain": [
				certificate1 as base64,
				certificate2 as base64 etc.
			],
			"type": "Dongle" or "Native"
		}
	]
}
```
### Example Request Body
```
{
    "type": "Dongle"
}
```

### Example Response
```
{
    "status": "success",
    "message": "Certificate Fetched Succesfully",
    "code": "200",
    "certificateList": [
        {
            "name": "CN=Muhammad Ahad Ul Alam,2.5.4.5=#132b4e494436313831626564363334366437656362323061646233333965636335356633653237396430316331,L=Dhaka,2.5.4.17=#130431323037,OU=CA Operations and Cyber Security,O=BCC,C=BD",
            "alias": "3C70BBE13F880766",
            "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqE94wbLYEWs6mevbf/0xlEqmwHrIfp4Qk9mljaMTnl97oXfUqgCCFxClUeE+9Jq8Dbk4aEm6TryI9cCUp+hwOxgvYEDePr2/6OtVqkaQY+QJHBEsjJhb6oBSQ0PW8Tz4hb38UgG+ZFFeSqDKIIoOj0+L4z4oVvI6y18RhRLk5Dr6a/cSxtyO32OKEg3RnBLriLBy6Mzo03PuI2YPOOJwaOSiNuY5U56aWIIoDFyHdkuKiqnTOVKqThkFjtamoGDzEoxF72f773kRZf/fUziy7ozhY8P0IGMj+IGL8FJek1K/Y/tj/LPL0i+5roOANahaZW68yJZ6wgxYDVfGa3zxswIDAQAB",
            "certificate": "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",
            "certificateChain": [
                "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"
            ],
            "type": "Dongle"
        }
    ]
}
```

### Request
`POST localhost:8089/api/sign-hash`

### Request Body
```
{
	"alias": alias,
	"hash":documentHash as Base64 string,
	"type":"Dongle" or "Native"
}
```
### Response
```
{
	"status": "success",
	"code": "200",
	"message": HashSigned Successfully,
	"hashResponse": hash response as base64 string
}
```
### Example Request Body
```
{
    "alias": "3C70BBE13F880766",
    "hash": "NVrzrkRo9e2Y1qmd4rTd0QxAzrrhTODG5thhlA4jdiA=",
    "type": "Dongle"
}
```
### Example Response
```
{
    "status": "success",
    "code": "200",
    "message": "HashSigned Successfully",
    "hashResponse": "QJLnCT2OAYokU4OmuB0w11dSG3qXwhluX2pBRm0QdphZGjKGMFnnMWZHe/MynevrYqwjE9ffipiH0nQU0hax7uWkk8Cr4FO4Y0UsSTtPf3xDU5QRJJT53kPU+ktsoOJ3zSoQFUOdcYYCbK4b2ZPpvMX2w57q+RO5rAl9R6LEsdkWz1O4JI+v5kmv8kE5T3b3cGu4i09FlqpfJeCbIluEvRGPmqdhKUahgLpOXMFVv59bCnxrt/1I59ltBm/PucokBw9Bour+fK+suWG/R9NLMQXya/DEIAwE1a1xWVsuRfh64khonD2lH8XVqeeOK28ecqUYPZpEQtwAl41zIwb1Zg=="
}
```
