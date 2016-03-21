import requests
import time
import sys
from json import JSONEncoder

numberOfReq = 1
try:
   numberOfReq = int(sys.argv[1])
except:
   print 'Defaulting to single request : ', sys.argv[1:]

url = 'http://vaipo.herokuapp.com'
action_call =  url + '/call'
action_userack = url + '/userack'
action_end = url + ''

#callerRegID = '37040911-8350-3033-814c-0d2254bb0b9b'
#caller = "8586106959"

callerRegID = '006e2bdc-7913-3178-b628-29e29d3807c0'
caller = '6504408319'

calleeRegID = 'f9b8e301-5d00-3dde-a5fe-fe4c4f110018'
callee = "65039072144"

CALL_STATE_DIAL = 1
CALL_STATE_INC = 2
CALL_STATE_END = 4


dialJson = {
	"id": callerRegID,
	"callee": callee,
	"caller": caller,
	"state": CALL_STATE_DIAL,
	"userAck": False,
	"receiveAck": False
}

incJson = {
	"id": calleeRegID,
	"callee": callee,
	"caller": caller,
	"state": CALL_STATE_INC,
	"userAck": False,
	"receiveAck": False
}

userackJson = {
	"id": callerRegID,
	"userAck": False,
	"receiveAck": False
}

endIncJson = {
	"id": calleeRegID,
	"callee": callee,
	"caller": caller,
	"state": CALL_STATE_END,
	"userAck": False,
	"receiveAck": False
}

endDialJson = {
	"id": callerRegID,
	"callee": callee,
	"caller": caller,
	"state": CALL_STATE_END,
	"userAck": False,
	"receiveAck": False
}

headers = { 'Accept' : 'application/json', 'Content-Type' : 'application/json'}
#r = requests.post(action_call, data=open('example.json', 'rb'), headers=headers)

print('-------------------------------------------------------------')
for x in xrange(0,numberOfReq ):
	print('############ Sending ' + str(x + 1) + '/' + str(numberOfReq) + ' request ###############')

	print('1. Sending Dial Req')
	dialReq = requests.post(action_call, json=dialJson, headers=headers)
	time.sleep(3)

	print('2. Sending Inc Req')
	incReq = requests.post(action_call, json=incJson, headers=headers)
	time.sleep(3)

	print('3. Sending Negative Ack')
	userAckReq = requests.post(action_userack, json=userackJson, headers=headers)
	time.sleep(1)

	print('4. End Inc Call')
	endIncReq = requests.post(action_call, json=endIncJson, headers=headers)
	time.sleep(1)
	print('5. End Dialed Call')
	endDialReq = requests.post(action_call, json=endDialJson, headers=headers)
	print('#############################################\n')
	time.sleep(1)
print('-------------------------------------------------------------')


