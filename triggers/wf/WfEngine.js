
var eb = bsf.lookupBean("siEnvironmentBean");
var sb = bsf.lookupBean("imServerBean");
var params = bsf.lookupBean("parametersBean");
var delta = bsf.lookupBean("imIssueDeltaBean");

var type = delta.getType();
var prop = getProperties(type);
var state = delta.getOldState();
var actionFieldName = prop.getProperty(state + ".action", "");
var roleFieldName = prop.getProperty(state + ".role", "");
var ownerFieldName = prop.getProperty("Engine.owner", "");
var ownerFieldName = prop.getProperty("Engine.owner", "");


function main() {
	if(!isActionChanged() && !isRoleChanged()){
		return;
	}
	processState();
	processOwner();
}

function isActionChanged(){
	var result = false;
	
	if(actionFieldName != "" && delta.isFieldChanged(actionFieldName)){
		result = true;
	}
	
	return result;
}

function isRoleChanged(){
	var result = false;
	
	if(roleFieldName != "" && delta.isFieldChanged(roleFieldName)){
		result = true;
	}
	
	return result;
}

function processState(){
	if(isActionChanged()){
		var action = delta.getNewFieldValue(actionFieldName);
		if(action != null){
			var nextState = prop.getProperty(state + "." + action, "");
			if(nextState != ""){
				delta.setState(nextState);
				clearAction(nextState);
				auditStateLog();
			}
		}
	}
}

function auditStateLog(){
	
}

function clearAction(nextState){
	var actionFieldName2 = prop.getProperty(nextState + ".action", "");
	if(actionFieldName2 != ""){
		var action = delta.getNewFieldValue(actionFieldName2);
		if(action != null || action != ""){
			delta.setFieldValue(actionFieldName2, null);
		}
	}

}

function processOwner(){
		if(delta.isStateChanged()){
			var newState = delta.getNewState();
			var roleFieldName2 = prop.getProperty(newState + ".role", "");
			if(roleFieldName2 != ""){
				var user = delta.getNewFieldValue(roleFieldName2);
				setOwner(user);
			}
		}else if(isRoleChanged()){
			var user = delta.getNewFieldValue(roleFieldName);
			setOwner(user);
		}
}

function setOwner(user){
	if(ownerFieldName != ""){
		delta.setFieldValue(ownerFieldName, user);
		auditReassginLog();
	}
}

function auditReassginLog(){
	
}

function getProperties(type){
	var properties = new java.util.Properties();
	
	var rd = eb.getInstallDirectory();
	var path = new java.io.File(rd + "/data/triggers/scripts/wf/" + type + ".properties");
    var inputStream = new java.io.BufferedInputStream(new java.io.FileInputStream(path));
    properties.load(new java.io.InputStreamReader(inputStream, "UTF-8"));
    inputStream.close();
    inputStream = null;
    
    return properties
}

main();
