angular.module('metrics', ['chart.js'])
    .controller('metricsController', function ($interval, $http) {
        var vm = this;
        vm.values = {};
        vm.cpuValues = [[]];
        vm.labels = [];
        vm.options = {
            scales: {
                yAxes: [{id: 'y-axis-1', type: 'linear', position: 'left', ticks: {min: 0, max:1}}]
            }
        };

        $interval(function () {
            $http.get('/values')
                .then(function (response) {
                    vm.values = response.data;
                    var labels = [];
                    var cpuValues = [];
                    angular.forEach(vm.values, function (value, key) {
                        var date = new Date(parseInt(key));
                        labels.push(date.getHours() + ':' + date.getMinutes());
                        cpuValues.push(value.cpuUsage);
                    });
                    vm.labels = labels;
                    vm.cpuValues = [cpuValues];
                });
        }, 2000);

    });