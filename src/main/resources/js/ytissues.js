var getYtBase = function() {
    var getYtServerUriInfo = function() {
        var httpGet = function(url) {
            var xmlHttp = new XMLHttpRequest();
                xmlHttp.open("GET", url, false);
                xmlHttp.send(null);
                return xmlHttp.responseText;
            };
        return AJS.$.parseJSON(httpGet(AJS.Confluence.getBaseUrl() + "/plugins/servlet/ytissues-ytbase"));
    };
    var uriParts = getYtServerUriInfo();
    var defaultProtoToPort = {"http": 80, "https": 443};
    uriParts.protocol = uriParts.protocol || "http";
    uriParts.port = uriParts.port || defaultProtoToPort[uriParts.protocol];
    uriParts.path = uriParts.path || "/";

    var isDefaultProtoPort = defaultProtoToPort[uriParts.protocol] == uriParts.port;
    return uriParts.protocol + "://" + uriParts.host + (isDefaultProtoPort ? "" : (":" + uriParts.port)) + uriParts.path;
};

var deleteLastSlashes = function(input) {
    var lastNonSlashPosition = 0;
    for (var i = 0; i < input.length; i++) {
        if (input.charAt(i) != "/")
            lastNonSlashPosition = i;
    }
    return input.substring(0, lastNonSlashPosition + 1);
};

var getKeyValuePairs = function(str) {
    // this is FSM with two states FST and BACKSLASH
    // key1=val1|key2=val2
    var state = "FST";
    var result = {};
    var currentKey = "";
    var currentStr = "";
    for (var i = 0; i < str.length; i++) {
        var ch = str.charAt(i);
        if (state == "FST") {
            if (ch == '\\') {
                state = "BACKSLASH";
            } else if (ch == '=') {
                currentKey = currentStr;
                currentStr = "";
            } else if (ch == '|') {
                result[currentKey] = currentStr;
                currentStr = "";
            } else {
                currentStr = currentStr + ch;
            }
        } else {
            state = "FST";
            currentStr = currentStr + ch;
        }
    }
    result[currentKey] = currentStr;
    return result;
};

var openInYt = function(e, macroElement) {
    var macroNode = AJS.$(macroElement);
    var rawParameters = macroNode.attr("data-macro-parameters");
    if (!rawParameters) {
        var mess = "YtIssues macro parameters undefined";
        alert(mess);
        console.error(mess);
        return;
    }

    var parameters = getKeyValuePairs(rawParameters);
    var issueIdOrUrl = parameters["issueIdOrUrl"];
    if (!issueIdOrUrl) {
        var mess = "YtIssues macro parameter 'issueIdOrUrl' undefined";
        alert(mess);
        console.error(mess);
        return;
    }


    var issueId;
    if (issueIdOrUrl.search(/^[A-Za-z0-9]+\-\d+$/) != -1)
        issueId = issueIdOrUrl;
    else {
        //Described in SingleYtMacro.java
        var urlRE = /^(?:([A-Za-z\.0-9-]+):\/\/)?(([^\/:]+|(?:\[[^\/]*\]))(?::(\w+))?)\/(?:[A-Za-z0-9_\.~!*'();@&=+$,?%\[\]-]*\/)*issue\/([A-Za-z0-9]+\-\d+)(?:#(.*))?$/;
        var matching = urlRE.exec(issueIdOrUrl);
        if (!matching || !matching[5]) {
            var mess = "Issue id not found in parameter: '" + issueIdOrUrl + "'";
            alert(mess);
            console.error(mess);
            return;
        }
        issueId = matching[5];
    }
    var base = deleteLastSlashes(getYtBase());
    var resultUrl = base + "/issue/" + issueId;
    window.open(resultUrl, "_blank");
};

AJS.Confluence.PropertyPanel.Macro.registerButtonHandler("openinyt", function(e, macroElement) {
    openInYt(e, macroElement);
});