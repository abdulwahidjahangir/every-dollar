# Every-Dollar

**Every-Dollar** is an expense tracking and management application designed to help users efficiently manage their finances.

## Features

1. **User Authentication & Registration**: 
   - Users can sign in via email and password, receiving a JWT token upon successful login. 
   - Users can sign up with first name, last name, email, and password, receiving a verification token.

2. **Account Verification**: 
   - Users can verify their account using a verification token sent via email. 
   - If needed, users can request a new verification email.

3. **Password Management**: 
   - Users can reset their password using a token sent to their email and update their password after signing in.

4. **User Information Management**: 
   - Users can view and update their account information, including first name, last name, and email.

5. **Category Management**: 
   - Users can manage expense categories, including creating, updating, and deleting categories. 
   - Categories can be filtered by date range and parent category.

6. **Expense Management**: 
   - Users can create, update, delete, and retrieve expenses. 
   - Expenses can be filtered by category, date range, and sorted by various fields.

7. **Pagination & Sorting**: 
   - For managing large datasets, users can paginate and sort expenses by various fields.

8. **Payment Integration**: 
   - Users can retrieve a payment intent (client secret) for processing payments and verify payments using a payment ID.

9. **CSRF Protection**: 
   - A CSRF token can be retrieved via an endpoint to protect against cross-site request forgery attacks, ensuring safe data-modifying requests.

10. **Error Handling**: 
    - Clear error messages are returned in case of invalid inputs or failed operations, such as failed authentication or invalid payment IDs.

## User Endpoints

1. **Sign In Request**  
   - **Endpoint**: POST /api/public/sign-in  
   - **Request Body**:
     ```json
     {
       "email": "string (required, non-empty, valid email format)",
       "password": "string (required, non-empty)"
     }
     ```
   - **Response**:
     - Success: JWT token and full name
     ```json
     {
       "jwtToken": "string",
       "fullName": "string"
     }
     ```
     - Failure: Error message
     ```json
     {
       "message": "Bad credentials",
       "status": false
     }
     ```

2. **Sign Up Request**  
   - **Endpoint**: POST /api/public/sign-up  
   - **Request Body**:
     ```json
     {
       "firstName": "string (required, non-empty, min 2 characters)",
       "lastName": "string (required, non-empty, min 2 characters)",
       "email": "string (required, non-empty, valid email format)",
       "password": "string (required, non-empty, min 6 characters)"
     }
     ```
   - **Response**:
     - Success: Registration confirmation with verification token
     ```json
     {
       "token": "User registered successfully! We have sent a verification link to your email. Please verify to continue."
     }
     ```

3. **Verify User Request**  
   - **Endpoint**: POST /api/public/verify  
   - **Request Body**:
     ```json
     {
       "token": "string (required, non-null)"
     }
     ```
   - **Response**:
     - Success: Verification confirmation
     ```json
     "User successfully verified"
     ```

4. **Get Verification Request**  
   - **Endpoint**: POST /api/public/get-verify  
   - **Request Body**:
     ```json
     {
       "email": "string (required, valid email format)"
     }
     ```
   - **Response**:
     - Success: Verification email request status
     ```json
     "If the email address matches an account in our system, we will send you a verification email"
     ```

5. **Forgot Password Request**  
   - **Endpoint**: POST /api/public/forgot-password  
   - **Request Body**:
     ```json
     {
       "email": "string (required, valid email format)"
     }
     ```
   - **Response**:
     - Success: Password reset email status
     ```json
     "If the email address matches an account in our system, we will send you a password reset email"
     ```

6. **Reset Password Request**  
   - **Endpoint**: POST /api/public/reset-password  
   - **Request Body**:
     ```json
     {
       "token": "string (required, non-null)",
       "password": "string (required, non-empty, min 6 characters)"
     }
     ```
   - **Response**:
     - Success: Password reset confirmation
     ```json
     "Your password has been successfully updated"
     ```

7. **Update Password Request**  
   - **Endpoint**: POST /api/user/update-password  
   - **Request Body**:
     ```json
     {
       "password": "string (required, non-empty, min 6 characters)"
     }
     ```
   - **Response**:
     - Success: Password update confirmation
     ```json
     "Your password has been successfully updated"
     ```

8. **Get User Info Request**  
   - **Endpoint**: GET /api/user/get-me  
   - **Response**:
     ```json
     {
       "firstName": "string",
       "lastName": "string",
       "email": "string",
       "isBlocked": boolean,
       "isVerified": boolean
     }
     ```

9. **Update User Info Request**  
   - **Endpoint**: POST /api/user/update-me  
   - **Request Body**:
     ```json
     {
       "firstName": "string (required, non-empty, min 2 characters)",
       "lastName": "string (required, non-empty, min 2 characters)",
       "email": "string (required, non-empty, valid email format)"
     }
     ```
   - **Response**:
     ```json
     {
       "firstName": "string",
       "lastName": "string",
       "email": "string",
       "isBlocked": boolean,
       "isVerified": boolean
     }
     ```

## Category Endpoints

1. **GET /api/user/categories**  
   - **Request Parameters**:
     - `parent-id` (optional): Long
     - `from` (optional): String (format: yyyy-MM-dd)
     - `to` (optional): String (format: yyyy-MM-dd)
   - **Response**:
     ```json
     [
       {
         "id": Long,
         "name": String,
         "total": BigDecimal
       }
     ]
     ```

2. **POST /api/user/categories**  
   - **Request Body**:
     ```json
     {
       "name": "string (required)",
       "parentId": Long (required)
     }
     ```
   - **Response**:
     ```json
     "New Category successfully created"
     ```

3. **PUT /api/user/categories**  
   - **Request Body**:
     ```json
     {
       "name": "string (required)",
       "id": Long (required)
     }
     ```
   - **Response**:
     ```json
     "Category has been successfully updated"
     ```

4. **DELETE /api/user/categories**  
   - **Request Parameters**:
     - `category-id`: Long (required)
   - **Response**:
     ```json
     "Category successfully deleted"
     ```

## Expense Endpoints

1. **GET /api/user/expenses**  
   - **Request Parameters**:
     - `category-id`, `page`, `page-size`, `sort-by`, `sort-direction`, `from`, `to` (optional)
   - **Response**:
     ```json
     {
       "data": {
         "expenseDTOs": [...],
         "pageNumber": 0,
         "pageSize": 10,
         "totalExpenses": 100,
         "totalPages": 10
       },
       "totalExpense": 250.50
     }
     ```

2. **POST /api/user/expenses**  
   - **Request Body**:
     ```json
     {
       "title": "string (required)",
       "description": "string (optional)",
       "categoryId": Long (required),
       "amount": BigDecimal (required),
       "transactionType": "string (required)",
       "transactionMethod": "string (required)",
       "notes": "string (optional)",
       "createdAt": "string (optional)"
     }
     ```
   - **Response**:
     ```json
     {
       "id": 1,
       "title": "Grocery shopping",
       "amount": 120.50,
       "transactionType": "expense",
       "transactionMethod": "card",
       "notes": "Bought fruits and vegetables",
       "createAt": "2024-12-08T12:00:00",
       "updatedAt": "2024-12-08T12:00:00"
     }
     ```

3. **PUT /api/user/expenses**  
   - **Request Parameters**:
     - `expense-id`: Long (required)
   - **Response**:
     ```json
     {
       "id": 1,
       "title": "Grocery shopping",
       "amount": 130.00,
       "transactionType": "expense",
       "transactionMethod": "card",
       "notes": "Bought fruits and vegetables",
       "createAt": "2024-12-08T12:00:00",
       "updatedAt": "2024-12-08T13:00:00"
     }
     ```

4. **DELETE /api/user/expenses**  
   - **Request Parameters**:
     - `expense-id`: Long (required)
   - **Response**:
     ```json
     {
       "message": "Expense successfully deleted"
     }
     ```

## Payment Endpoints

1. **GET /api/user/get-payment-intent**  
   - **Response**:
     ```json
     {
       "client-secret": "your_payment_intent_client_secret"
     }
     ```

2. **POST /api/user/verify-payment**  
   - **Request Parameters**:
     - `payment-id`: String (required)
   - **Response**:
     ```json
     "Thanks for your donation"
     ```

## CSRF Token Endpoint

1. **GET /api/public/csrf-token**  
   - **Response**:
     ```json
     {
       "token": "CSRF-TOKEN-1234567890"
     }
     ```
---

This version should work well for a complete API documentation. Let me know if you need any more adjustments!
