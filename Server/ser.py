import web
import time
import json
import hashlib
import random as rand
from math import sin, cos, sqrt, atan2
import os

'''
 TODO:
 1. 修改信息可以改哪些信息 
    Chengsheng: password，email, avatar, DOB
 2. discover胶囊的时候，需要优先显示自己的胶囊么 
    Chengsheng: 可以搞成(当前地图位置)的(多少范围)内的随机显示(maximum多少个)的，capsule
'''

urls = [
    '/signIn', 'SignIn',
    '/signUp', 'SignUp',
    '/signOut', 'SignOut',
    '/changeAvatar', 'ChangeAvatar',
    '/changePassword', 'ChangePassword',
    '/getProfile', 'GetProfile',
    '/createCapsule', 'CreateCapsule',
    '/discoverCapsule', 'DiscoverCapsule',
    '/openCapsule', 'OpenCapsule',
    '/getCapsuleHistory', 'GetCapsuleHistory',
    '/uploadImage', 'UploadImage',
    '/uploadAudio', 'UploadAudio',
    '/uploadAvatar', 'UploadAvatar'
]

app = web.application(urls, globals())
db = web.database(dbn='sqlite', db='test.db')

db.query('''
    CREATE TABLE IF NOT EXISTS users(
        uid INTEGER PRIMARY KEY,        -- Unique ID
        uusr TEXT NOT NULL,             -- Username 
        upwd TEXT NOT NULL,             -- Password
        uavatar TEXT,                   -- Avatar URI
        uemail TEXT NOT NULL,           -- Email
        udob TEXT NOT NULL,             -- Date of birth
        utme INTEGER,                   -- Last time when login
        utkn TEXT                       -- User's token
    );
''')

db.query('''
    CREATE TABLE IF NOT EXISTS capsules(
        cid INTEGER PRIMARY KEY,        -- Unique ID
        cusr TEXT NOT NULL,             -- User who create the capsule
        ctime TEXT NOT NULL,            -- When create the capsule
        cpermission INTEGER NOT NULL,   -- Public or private
        clat REAL NOT NULL,             -- Latitude 
        clon REAL NOT NULL,             -- Longitude
        ctitle TEXT NOT NULL,         -- Capsule title
        ccontent TEXT NOT NULL,         -- Capsule content
        cimage TEXT,                    -- Capsule image URI
        caudio TEXT,                    -- Capsule audio URI
        ccount INTEGER,                 -- Count of capsule being viewed
        CONSTRAINT fk_users
        FOREIGN KEY (cusr)
        REFERENCES users(uusr)
    );
''')

db.query('''
    CREATE TABLE IF NOT EXISTS capsules_history(
        hid INTEGER PRIMARY KEY,        -- Unique ID
        husr TEXT NOT NULL,             -- User who discover the capsule
        hcap INTEGER NOT NULL,          -- Capsule is discovered
        hlat REAL NOT NULL,             -- Latitude 
        hlon REAL NOT NULL,             -- Longitude
        htime INTEGER NOT NULL,         -- When capsule is discovered
        CONSTRAINT fk_users
        FOREIGN KEY (husr)
        REFERENCES users(uusr),
        CONSTRAINT fk_capsules
        FOREIGN KEY (hcap)
        REFERENCES capsules(cid)
    );
''')

def genToken():
    r = rand.random()
    return str(hashlib.sha1(('%f%s'%(r, time.ctime())).encode('utf-8')).hexdigest())

def getUser(usr, tkn=None):
    if tkn:
        d = dict(tkn = tkn); q = 'utkn = $tkn'
    else:
        d = dict(usr = usr); q = 'uusr = $usr'

    res = list(db.select('users', d, where=q))
    return res[0] if len(res) else None

def getEmail(email, tkn=None):
    if tkn:
        d = dict(tkn = tkn); q = 'utkn = $tkn'
    else:
        d = dict(email = email); q = 'uemail = $email'

    res = list(db.select('users', d, where=q))
    return res[0] if len(res) else None

def getPwd(usr, pwd):
    return hashlib.sha1(('%s:%s'%(usr, pwd)).encode('utf-8')).hexdigest()

def checkToken(user):
    t = int(time.time())
    expired_day = 15
    # Token expires within $expired_day days
    if t - user['utme'] > expired_day * 24 * 60 * 60:
        return False
    return True

def getUserInfo(user):
    res = {}
    keys = ['uusr', 'uavatar', 'uemail', 'udob']
    for key in keys:
        res[key] = user[key]
    return res

def getCapsuleInfo(capsule):
    res = {}
    usr = capsule['cusr']
    user = getUser(usr)
    keys = ['cid', 'cusr', 'ccontent', 'ctitle', 'cimage', 'caudio', 'ccount']
    for key in keys:
        res[key] = capsule[key]
    res['cavatar'] = user['uavatar']
    return res

def json_response(f):
    def wrapper(*args, **kwargs):
        res = f(*args, **kwargs)

        if type(res) is dict:
            return json.dumps(res)
        else:
            return res
    return wrapper

def get_distance(lat1, lon1, lat2, lon2):
    # Approximate radius of earth in km
    R = 6373.0
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    distance = R * c

def filter_capsule(lat, lon, capsules, max_distance = 5):
    # Capsules within max distance can be discovered 
    res = []
    for capsule in capsules:
        if get_distance(lat, lon, capsule['lat'], capsule['lon']) <= max_distance:
            res.append(capsule)
    return res

class SignUp:
    def POST(self):
        webData = web.data().decode()
        i = json.loads(webData)
        # Check form completeness
        if not i.get('usr') or not i.get('pwd') or \
        not i.get('email') or not i.get('dob'):
            return web.badrequest()

        usr = i.get('usr')
        email = i.get('email')
        pwd = getPwd(usr, i.get('pwd'))
        avatar = i.get('avatar')
        dob = i.get('dob')

        # Check whether user exist
        if getUser(usr) or getEmail(email):
            return {'error': 'userExist - user already exist'}

        # Add new user's record into database
        res = db.insert('users', uusr=usr, upwd=pwd, uemail=email, \
            uavatar=avatar, udob=dob, utkn=genToken(), utme=int(time.time()))

        # Crate token, and update it in database
        user = getUser(usr)
        t = int(time.time())
        token = genToken()
        res = db.update('users', where='uid=$id', utkn=token, \
            utme=int(time.time()), vars={'id':user['uid']})

        # return token and user's info
        return {'success':True, 'token':token, 'userInfo': getUserInfo(user)}

class SignIn:
    def POST(self):
        webData = web.data().decode()
        i = json.loads(webData)
        # Check for form completeness
        if not i.get('usr') or not i.get('pwd'):
            return web.badrequest()
        usr = i.get('usr')
        pwd = getPwd(usr, i.get('pwd'))

        # Check whether user exist
        user = getUser(usr)
        if not user:
            return {'error': 'userNotExist - user does not exist'}

        # Check whether password is correct
        if not user['upwd'] == pwd:
            return {'error': 'invalidPass - invalid password, try again'}
        else:
            # Update token
            token = genToken()
            res = db.update('users', where='uid=$id', utkn=token, \
                utme=int(time.time()), vars={'id':user['uid']})
            # Return token and user's info
            return {'success':True, 'token':token, 'userInfo': getUserInfo(user)}
        return {'error':'loginError - cannot login'}

class SignOut:
    def POST(self):
        webData = web.data().decode()
        i = json.loads(webData)

        # Check whether request contain token 
        if not i.get('tkn'):
            return web.badrequest()
        tkn = i.get('tkn')
        user = getUser(None, tkn)

        # Check whether the user has logged in
        if not user:
            return {'error':'Not logged in'}

        # Update token as none in databse
        res = db.update('users', where='uid=$id', utkn=None, vars={'id':user['uid']})

        return {'success': True}

class ChangeAvatar:
    def POST(self):
        webData = web.data().decode()
        i = json.loads(webData)

        # Check whether request contain token 
        if not i.get('tkn') or not i.get('avatar'):
            return web.badrequest()

        tkn = i.get('tkn')
        avatar = i.get('avatar')

        # Check whether the user has logged in
        user = getUser(None, tkn)

        if not user['utkn']:
            return {'error':'Not logged in'}

        if not checkToken(user):
            return {'error':'Token expired'}

        # Update user's info and return user's info        
        res = db.update('users', where='uid=$id', uavatar=avatar, vars={'id':user['uid']})
        # Since info might be changed, get user's latest info again
        user = getUser(None, tkn)

        return {'success': True, 'userInfo': getUserInfo(user)}

class ChangePassword:
    def POST(self):
        webData = web.data().decode()
        i = json.loads(webData)

        # Check whether request contain token 
        if not i.get('tkn') or not i.get('oldpass') \
        or not i.get('newpass'):
            return web.badrequest()

        tkn = i.get('tkn')
        opass = i.get('oldpass')
        npass = i.get('newpass')

        # Check whether the user has logged in
        user = getUser(None, tkn)
        if not user:
            return {'error':'Not logged in'}

        usr = user.get('uusr')
        pwd = getPwd(usr, opass)

        if not checkToken(user):
            return {'error':'Token expired'}

        # Check whether password is correct
        if not user['upwd'] == pwd:
            return {'error': 'invalidPass - invalid password, try again'}

        # Update user's password and return user's info   
        pwd = getPwd(usr, npass)     
        res = db.update('users', where='uid=$id', upwd=pwd, vars={'id':user['uid']})

        return {'success': True, 'userInfo': getUserInfo(user)}

class GetProfile:
    @json_response
    def GET(self):
        i = web.input()

        # Check whether request contain token 
        if not i.get('tkn'):
            return web.badrequest()
        tkn = i.get('tkn')

        # Check whether the user has logged in
        user = getUser(None, tkn)
        if not user:
            return {'error':'Not logged in'}

        if not checkToken(user):
            return {'error':'Token expired'}

        # Return user's info
        return {'success': True, 'userInfo': getUserInfo(user)}

class CreateCapsule:
    def POST(self):
        webData = web.data().decode()
        i = json.loads(webData)

        # Check whether request contain token 
        if not i.get('tkn') or not i.get('content') or \
        not i.get('lat') or not i.get('lon') or not i.get('time')\
        or (i.get('permission') is None) or not i.get('title'):
            return web.badrequest()
        tkn = i.get('tkn')
        user = getUser(None, tkn)

        # Check whether the user has logged in
        if not user:
            return {'error':'Not logged in'}

        if not checkToken(user):
            return {'error':'Token expired'}

        usr = user.get('uusr')
        content = i.get('content') 
        title = i.get('title') 
        lat = i.get('lat') 
        lon = i.get('lon') 
        tim = i.get('time') 
        permission = i.get('permission') 
        img = i.get('img') 
        audio = i.get('audio') 

        # Add new capsule into database
        res = db.insert('capsules', cusr=usr, ctime=tim, cpermission=permission, \
            clat=lat, clon=lon, ccontent=content, ctitle=title, cimage=img, \
            caudio=audio, ccount=0)
        return {'success': True}

class DiscoverCapsule:
    @json_response
    def GET(self):
        i = web.input()

        # Check whether request contain token 
        if not i.get('tkn') or not i.get('lon') or not i.get('lat'):
            return web.badrequest()
        tkn = i.get('tkn')

        # Check whether the user has logged in
        user = getUser(None, tkn)
        if not user:
            return {'error':'Not logged in'}

        if not checkToken(user):
            return {'error':'Token expired'}

        lat = i.get('lat')
        lon = i.get('lon')
        usr = user.get('uusr')

        vars = dict(cusr=usr, cpermission=0)
        self_capsule = db.select('capsules', where="cusr = $cusr", vars=vars)
        other_capsules = db.query("SELECT * FROM capsules WHERE cusr != '{}' AND cpermission != 0".format(usr))

        all_capsules = list(self_capsule)
        all_capsules.extend(list(other_capsules))

        # Remove visited capsules by this user
        visited_capsules_res = db.query("SELECT hcap FROM capsules_history WHERE husr = '{}'".format(usr))
        visited_capsules = [int(c['hcap']) for c in visited_capsules_res]
        for capsule in all_capsules:
            if capsule['cid'] in visited_capsules:
                all_capsules.remove(capsule)

        max_distance = i.get('max_distance') if i.get('max_distance') else 5
        num_capsules = i.get('num_capsules') if i.get('num_capsules') else 20

        # Filter the capsules within max_distance
        filtered_capsule = filter_capsule(lat, lon, other_capsules, max_distance)
        # Retieve num_caplsules capsules randomly
        if len(all_capsules) > num_capsules:
            retrieved_capsules = rand.sample(all_capsules, num_capsules)
        else:
            retrieved_capsules = all_capsules

        res_capsules =  []
        if len(retrieved_capsules):
            for capsule in retrieved_capsules:
                res_capsules.append(getCapsuleInfo(capsule))
        return {'success': True, 'capsules': res_capsules}

class OpenCapsule:    
    def POST(self):
        webData = web.data().decode()
        i = json.loads(webData)

        # Check whether request contain token 
        if not i.get('tkn') or not i.get('cid') or \
        not i.get('lat') or not i.get('lon') or not i.get('time'):
            return web.badrequest()

        tkn = i.get('tkn')
        user = getUser(None, tkn)
        lat = i.get('lat') 
        lon = i.get('lon') 
        tim = i.get('time') 

        # Check whether the user has logged in
        if not user:
            return {'error':'Not logged in'}

        if not checkToken(user):
            return {'error':'Token expired'}

        usr = user.get('uusr')
        cid = int(i.get('cid'))

        # Update table capsules
        vars = dict(cid=cid)
        res_capsule = db.select('capsules', where="cid = $cid", vars=vars)[0]
        res_capsule['ccount'] += 1
        cnt = res_capsule.get('ccount') + 1
        res = db.update('capsules', where='cid=$id', ccount=cnt, vars={'id':cid})
        # Update table capsules_history
        res = db.insert('capsules_history', husr=usr, hcap=cid, \
            hlat=lat, hlon=lon, htime=tim)

        cur_capsule = db.query("SELECT * FROM capsules WHERE cid = '{}'".format(cid))[0]
        return {'success': True, 'capsule': getCapsuleInfo(cur_capsule)}

class GetCapsuleHistory:
    @json_response
    def GET(self):
        i = web.input()

        # Check whether request contain token 
        if not i.get('tkn'):
            return web.badrequest()
        tkn = i.get('tkn')

        # Check whether the user has logged in
        user = getUser(None, tkn)
        if not user:
            return {'error':'Not logged in'}

        if not checkToken(user):
            return {'error':'Token expired'}

        usr = user['uusr']

        capsules_his = db.query("SELECT * FROM capsules_history where husr='{}' ORDER BY\
         htime DESC".format(usr))
        res = []
        cid_list = []
        for his in capsules_his:
            cur_cid = his['hcap']
            if cur_cid in cid_list:
                continue
            # Avoid duplicated 
            cid_list.append(cur_cid)
            cur_capsule = db.query("SELECT * FROM capsules WHERE cid = '{}'".format(cur_cid))[0]
            res.append(getCapsuleInfo(cur_capsule))
        return {'success': True, 'hisotry': res}

class UploadImage:
    def POST(self):
        i = web.input(myfile={})
        try:
            tkn = i['tkn']
            user = getUser(None, tkn)
            # Check whether the user has logged in
            if not user:
                return {'error':'Not logged in'}

            if not checkToken(user):
                return {'error':'Token expired'}

            usr = user.get('uusr')

            # filename
            filename = i['myfile'].filename 
            format_name = filename.split(".")[-1]
            if format_name not in ['jpg', 'jpeg', 'png', 'gif', 'tif', 'psd', 'dng', 'cr2', 'nef']:
                return {'error': 'Invalid format'}
            target_folder = '/home/sudokuServer/static/mobile/'
            target_filename = str(usr) + '-' + str(int(time.time())) + '.' + format_name
            target_dir = os.path.join(target_folder, target_filename)
            f = open(target_dir, 'wb')
            f.write(i['myfile'].value)
            f.close()
            static_url = "https://www.tianzhipengfei.xin/static/mobile/" + target_filename
            return {"success": True, "file": static_url}
        except AttributeError as err:
            print("AttributeError: {0}".format(err))
            return web.badrequest()
        except:
            return web.badrequest()
        

class UploadAudio:
    def POST(self):
        i = web.input(myfile={})
        try:
            tkn = i['tkn']
            user = getUser(None, tkn)
            # Check whether the user has logged in
            if not user:
                return {'error':'Not logged in'}

            if not checkToken(user):
                return {'error':'Token expired'}

            usr = user.get('uusr')

            # filename
            filename = i['myfile'].filename 
            format_name = filename.split(".")[-1]
            if format_name not in ['wav', 'mp3', 'aac', 'amr']:
                return {'error': 'Invalid format'}
            target_folder = '/home/sudokuServer/static/mobile/'
            target_filename = str(usr) + '-' + str(int(time.time())) + '.' + format_name
            target_dir = os.path.join(target_folder, target_filename)
            f = open(target_dir, 'wb')
            f.write(i['myfile'].value)
            f.close()
            static_url = "https://www.tianzhipengfei.xin/static/mobile/" + target_filename
            return {"success": True, "file": static_url}
        except AttributeError as err:
            print("AttributeError: {0}".format(err))
            return web.badrequest()
        except:
            return web.badrequest()

class UploadAvatar:
    def POST(self):
        i = web.input(myfile={})
        try:
            usr = i['usr']
            if getUser(usr):
                return {'error': 'userExist - user already exist'}

            # filename
            filename = i['myfile'].filename 
            format_name = filename.split(".")[-1]
            if format_name not in ['jpg', 'jpeg', 'png', 'gif', 'tif', 'psd', 'dng', 'cr2', 'nef']:
                return {'error': 'Invalid format'}
            target_folder = '/home/sudokuServer/static/mobile/'
            target_filename = str(usr) + '-' + str(int(time.time())) + '.' + format_name
            target_dir = os.path.join(target_folder, target_filename)
            f = open(target_dir, 'wb')
            f.write(i['myfile'].value)
            f.close()
            static_url = "https://www.tianzhipengfei.xin/static/mobile/" + target_filename
            return {"success": True, "file": static_url}
        except AttributeError as err:
            print("AttributeError: {0}".format(err))
            return web.badrequest()
        except:
            return web.badrequest()
        


if __name__=='__main__':
    app.run()
