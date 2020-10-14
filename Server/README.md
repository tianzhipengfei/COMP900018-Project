# Server code

## Requirements
* python==3.x (Let's move on to python 3 if you still use python 2)
* web.py==0.61

## Front-end attention
1. Please validate the form before sending request. E.g. email validation, old pass != new pass
2. Avoid repeat post submission in a very short time
3. check file format before uploading

## API

### 1. SignUp
* URL: https://www.tianzhipengfei.xin/mobile/signUp
* Method: POST
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| usr | string | yes | username |
| pwd | string | yes | password |
| email | string | yes | email |
| dob | string | yes | date of birth |
| avatar | string | no | avatar |
* Returns:
	* Success: {'success': True}
	* Fail:
		* if username or email exists, return {'error': 'userExist - user already exist'}
		* if form is invalid,return web.badrequest()

### 2. SignIn
* URL: https://www.tianzhipengfei.xin/mobile/signIn
* Method: POST
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| usr | string | yes | username |
| pwd | string | yes | password |
* Returns:
	* Success: {'success': True, 'token': '48185039ab0b7cb57072bfcf64b0702c4eb5249b', 'userInfo': {'uusr': 'test', 'uavatar': None, 'uemail': 'test@gmail.com', 'udob': '2020-01-01'}}
	* Fail:
		* if username doesn't exists, return {'error': 'userNotExist - user does not exist'}
		* if password is incorrect, return {'error': 'invalidPass - invalid password, try again'}
		* if form is invalid,return web.badrequest()
		* Other fails: return {'error':'loginError - cannot login'}
		
### 3. SignOut
* URL: https://www.tianzhipengfei.xin/mobile/signOut
* Method: POST
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| tkn | string | yes | token |
* Returns:
	* Success: {'success':True}
	* Fail:
		* if user has not logged in, return {'error':'Not logged in'}
		* if form is invalid,return web.badrequest()

### 4. ChangeAvatar
* URL: https://www.tianzhipengfei.xin/mobile/changeAvatar
* Method: POST
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| tkn | string | yes | token |
| avatar | string | yes | avatar |
* Returns:
	* Success: {'sucess': True, 'userInfo': {'uusr': 'test', 'uavatar': 'test_avatar', 'uemail': 'test@qq.com', 'udob': '2020-01-01'}}
	* Fail:
		* if user has not logged in, return {'error':'Not logged in'}
		* if form is invalid,return web.badrequest()

### 5. ChangePassword
* URL: https://www.tianzhipengfei.xin/mobile/changePassword
* Method: POST
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| tkn | string | yes | token |
| oldpass | string | yes | old password |
| newpass | string | yes | new password |
* Returns:
	* Success: {'sucess': True, 'userInfo': {'uusr': 'test', 'uavatar': 'test_avatar', 'uemail': 'test@qq.com', 'udob': '2020-01-01'}}
	* Fail:
		* if old password is incorrect, return {'error': 'invalidPass - invalid password, try again'}
		* if user has not logged in, return {'error':'Not logged in'}
		* if form is invalid,return web.badrequest()
		
### 6. GetProfile
* URL: https://www.tianzhipengfei.xin/mobile/getProfile
* Method: GET
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| tkn | string | yes | token |
* Returns:
	* Success: {"success": true, "userInfo": {"uusr": "test", "uavatar": "test_avatar", "uemail": "test@qq.com", 'udob': '2020-01-01'}}
	* Fail:
		* if user has not logged in, return {'error':'Not logged in'}
		* if form is invalid,return web.badrequest()

### 7. CreateCapsule
* URL: https://www.tianzhipengfei.xin/mobile/createCapsule
* Method: POST
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| tkn | string | yes | token |
| content | string | yes | capsule's content |
| title | string | yes | capsule's title |
| lat | double | yes | latitude |
| lon | double | yes | longitude |
| time | string | yes | when create the capsule |
| permission | int | yes | Public(1) or private(0) |
| img | string | no | image's URL |
| audio | string | no | audio's URL |
* Returns:
	* Success: {'success': True}
	* Fail:
		* if user has not logged in, return {'error':'Not logged in'}
		* if form is invalid,return web.badrequest()

### 8. DiscoverCapsule
* URL: https://www.tianzhipengfei.xin/mobile/discoverCapsule
* Method: GET
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| tkn | string | yes | token |
| lat | double | yes | latitude |
| lon | double | yes | longitude |
| max_distance | int | no | maximum distance allowed to discover capsules (default 5) |
| num_capsules | int | no | maximum number of capsules can be found (default 20) |
* Returns:
	* Success: {"sucess": true, "capsules": [{"cid": 2, "cusr": "test1", "ccontent": "Test content1", "ctitle": "Test title", "cimage": null, "caudio": null, "ccount": 0, "cavatar": null}, {"cid": 3, "cusr": "test1", "ccontent": "Test content2", "ctitle": "Test title", "cimage": null, "caudio": null, "ccount": 0, "cavatar": null}]}
	* Fail:
		* if user has not logged in, return {'error':'Not logged in'}
		* if form is invalid,return web.badrequest()

### 9. OpenCapsule
* URL: https://www.tianzhipengfei.xin/mobile/openCapsule
* Method: POST
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| tkn | string | yes | token |
| cid | string | yes | capsule's id |
| lat | double | yes | latitude |
| lon | double | yes | longitude |
| time | string | yes | when open the capsule |capsules can be found (default 20) |
* Returns:
	* Success: {'success': True, 'capsule': {'cid': 1, 'cusr': 'test', 'ccontent': 'test content', 'ctitle': 'test title', 'cimage': None, 'caudio': None, 'ccount': 2, 'cavatar': None}}		* Fail:
		* if user has not logged in, return {'error':'Not logged in'}
		* if form is invalid,return web.badrequest()


### 10. GetCapsuleHistory
* URL: https://www.tianzhipengfei.xin/mobile/getCapsuleHistory
* Method: GET
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| tkn | string | yes | token |
* Returns:
	* Success: {"sucess": true, "hisotry": [{"cid": 1, "cusr": "test", "ccontent": "Test content", "ctitle": "Test title", "cimage": null, "caudio": null, "ccount": 2, "cavatar": "test_avatar"}, {"cid": 2, "cusr": "test1", "ccontent": "Test content1", "ctitle": "Test title", "cimage": null, "caudio": null, "ccount": 2, "cavatar": null}]}
	* Fail:
		* if user has not logged in, return {'error':'Not logged in'}
		* if form is invalid,return web.badrequest()

### 11. UploadImage
* URL: https://www.tianzhipengfei.xin/mobile/uploadImage
* Method: POST
* Header: {"enctype": "multipart/form-data", "Content-Type": "multipart/form-data"}
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| tkn | string | yes | token |
| myfile | file | yes |file to upload |
* Returns:
	* Success: {'success': True, 'file': 'https://www.tianzhipengfei.xin/static/mobile/test1-1601945632.png'}
	* Fail:
		* if user has not logged in, return {'error':'Not logged in'}
		* if file format is invalid, return {'error': 'Invalid format'}
		* if form is invalid,return web.badrequest()

### 12. UploadAudio
* URL: https://www.tianzhipengfei.xin/mobile/uploadAudio
* Method: POST
* Header: {"enctype": "multipart/form-data", "Content-Type": "multipart/form-data"}
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| tkn | string | yes | token |
| myfile | file | yes |file to upload |
* Returns:
	* Success: {'success': True, 'file': 'https://www.tianzhipengfei.xin/static/mobile/test1-1601945632.wav'}
	* Fail:
		* if user has not logged in, return {'error':'Not logged in'}
		* if file format is invalid, return {'error': 'Invalid format'}
		* if form is invalid,return web.badrequest()

### 13. UploadAvatar
* URL: https://www.tianzhipengfei.xin/mobile/uploadAvatar
* Method: POST
* Header: {"enctype": "multipart/form-data", "Content-Type": "multipart/form-data"}
* Parameters:

| Property | Type | Required | Description |
|---|---|---|---|
| usr | string | yes | username |
| myfile | file | yes |file to upload |
* Returns:
	* Success: {'success': True, 'file': 'https://www.tianzhipengfei.xin/static/mobile/test1-1601945632.png'}
	* Fail:
		* if username or email exists, return {'error': 'userExist - user already exist'}		* if file format is invalid, return {'error': 'Invalid format'}
		* if form is invalid,return web.badrequest()
