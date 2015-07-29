(function() {
    AJS.bind("init.rte", function() {
        var getYtServerUriInfo = function() {
            var httpGet = function(url) {
                var xmlHttp = new XMLHttpRequest();
                xmlHttp.open("GET", url, false);
                xmlHttp.send(null);
                return xmlHttp.responseText;
            };
            return AJS.$.parseJSON(httpGet(AJS.Confluence.getBaseUrl() + "/plugins/servlet/ytissues-ytbase"));
        };

        var deleteLastSlashes = function(input) {
            var lastNonSlashPosition = -1;
            for (var i = 0; i < input.length; i++) {
                if (input.charAt(i) != "/")
                    lastNonSlashPosition = i;
            }
            return input.substring(0, lastNonSlashPosition + 1);
        };
        
        var pasteHandler = function(uri, node, done) {
            var protoToPort = { 'http': 80, 'https': 443 };

            uri.protocol = uri.protocol || "http";
            uri.port     = uri.port || protoToPort[uri.protocol];

            var ytServerInfo = getYtServerUriInfo();
            ytServerInfo.protocol = ytServerInfo.protocol || "http";
            ytServerInfo.port     = ytServerInfo.port || protoToPort[ytServerInfo.protocol];

            var ytServerIssuePath = deleteLastSlashes(ytServerInfo.path) + "/issue/";

            if (uri.protocol == ytServerInfo.protocol && uri.host == ytServerInfo.host
                    && uri.port == ytServerInfo.port && uri.path.indexOf(ytServerIssuePath) == 0) {
                var issueIdRE = /^([A-Za-z0-9]+\-\d+)$/;
                var issueIdPart = uri.path.substring(ytServerIssuePath.length);
                var matching = issueIdRE.exec(issueIdPart);
                if (matching) {
                    var issueId = matching[1];
                    var macro = {name: 'single-yt-issue-macro', params: { issueIdOrUrl: issueId }};
                    tinymce.plugins.Autoconvert.convertMacroToDom(macro, done, done);
                } else
                    done();
            } else
                done();
        };
        tinymce.plugins.Autoconvert.autoConvert.addHandler(pasteHandler);
    });
})();