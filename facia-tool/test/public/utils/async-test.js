define([
    'test/utils/collections-loader',
    'test/utils/config-loader',
    'knockout',
    'utils/mediator'
], function(
    collectionsLoader,
    configLoader,
    ko,
    mediator
){
    // Redefine the 'it' method so it waits for the loader
    var currentTesting, running;
    var loaders = {
        'collections': collectionsLoader,
        'config': configLoader
    };

    return function (what, description, test) {

        it(description, function (done) {
            if (currentTesting !== what) {
                if (running) {
                    ko.cleanNode(window.document.body);
                    running.unload();
                    _.once.reset();
                    mediator.removeAllListeners();
                }
                currentTesting = what;
                running = loaders[what]();
            }
            running.loader.then(function () {
                test(done);
            });
        });
    };
});
