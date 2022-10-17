jQuery(function($, undefined) {
    var rootContext = (location.pathname).replace(
        /.index.html$/, "").replace(/[/]+/,"/");

    var stompClient = null;
    var connectionLossReported=false;
    var silentMode = false;

    function connect() {
        if (stompClient != null) {
            return;
        }

        var socket = new SockJS('socket', null,
           { 'transports': ['websocket', 'xdr-streaming', 'xhr-streaming',
               'iframe-eventsource', 'iframe-htmlfile', 'xdr-polling', 'xhr-polling',
               'iframe-xhr-polling', 'jsonp-polling']
            });
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function(frame) {
                //setConnected(true);
                console.log('Connected: ' + frame);
                connectionLossReported = false;
                stompClient.subscribe('/command', function(output){
                    received(JSON.parse(output.body));
                });
                received({result: '[[b;green;]Connected].\n'});
                silentMode = true;
                executeCommand("?");
            }, (error) => {
                stompClient = null;
                //setConnected(false);
                if(!connectionLossReported) {
                    var errMsg="[[b;red;black]" + error + "]";
                    received({result: errMsg});
                }
                connectionLossReported = true
                setTimeout(connect, 5000);
            }
        );
    }

    function executeCommand(command) {
        if(stompClient == null) {
            var message = 'No connection to server.';
            console.log(message);
            terminal.echo(message);
        }
        if (command.length > 0) {
            console.log('sendCommand: ' + command);
            stompClient.send(rootContext+'/command/send',
               {}, JSON.stringify({ 'input': command }));
        }
    }

    var terminal = $('body').terminal(function(command) {
            if (command !== '') {
                executeCommand(command);
            }
        }, {
            greetings: 'Web Shell',
            name: 'web-shell',
            historySize: 2048,
            prompt: '$ '
        });

    function received(jsonMsg) {
        if(jsonMsg.cwd) {
            var shell="";
            if(jsonMsg.shell) {
                shell=jsonMsg.shell+" ";
            }
            terminal.set_prompt(jsonMsg.host+":"+jsonMsg.cwd + "\n"+shell+"$ ");
        }

        if(silentMode) {
            silentMode = false;
            return;
        }
        console.log('Received: ' + JSON.stringify(jsonMsg));
        terminal.echo(jsonMsg.result);
     }

     connect();
});