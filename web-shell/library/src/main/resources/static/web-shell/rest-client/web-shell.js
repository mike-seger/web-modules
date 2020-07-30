jQuery(function($, undefined) {
    var silentMode = false;

    function start() {
        received({result: '[[b;green;]Started].\n'});
        silentMode = true;
        executeCommand("?");
    }

    function createFetchData(command) {
        return {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                input: command,
                created: Math.round(new Date().getTime() / 1000)
            })
        }
    }

    function executeCommand(command) {
        if (command.length > 0) {
            console.log('sendCommand: ' + command);
            fetch("execute", createFetchData(command))
            .then((resp) => resp.json())
            .then(function(data) { received(data); })
            .catch(function(error) {
                var message = "Error executing command - "+error;
                console.log(message);
                received({
                    dateTime: new Date(),
                    result: "[[b;red;black]" + message + "]"
                });
            });
        }
    }

    function logged(json) {
        console.log("JSON: "+JSON.stringify(json));
        return json;
    }

    var terminal = $('body').terminal(function(command) {
            if (command !== '') {
                executeCommand(command);
            }
        }, {
            greetings: 'Web Shell',
            name: 'web-shell',
            prompt: '$ ',
            historySize: 2048,
            tabcompletion: true,
            completion: function(command) {
                console.log(this.get_command()+" -> "+command);
                return [];
//                    fetch('complete', createFetchData(command))
//                    .then(res => logged(res.json()))
//                    .catch(function(error) {
//                        console.log(error);
//                    })
//                    ;
            }
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

     start();
});