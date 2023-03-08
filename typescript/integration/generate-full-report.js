/*
 * Usage: node generate-full-report.js ${PROJECT_NAME} ${VERSION} ${CURRENT_GIT_HASH}
 */

const report = require('multiple-cucumber-html-reporter');

const args = process.argv.slice(2);
console.log(args);

projectName = args[0]
release     = args[1]
gitSHA      = args[2]
startTime   = args[3]
endTime     = args[4]

title = 'Integration Test Run'

report.generate({
    jsonDir: '../../java/gms/integration/reports',
    reportPath: '../../test/results/',
    reportName: title.concat(' for ').concat(projectName).concat(' [').concat(release).concat('] (').concat(gitSHA).concat(')'),
    pageTitle: projectName.concat(' [').concat(release).concat('] ').concat(gitSHA),
    metadata:{
        device: process.env.HOSTNAME,
        platform: {
            name: process.platform,
        }
    },
    customData: {
        title: 'Run Information',
        data: [
            {label: 'Project', value: projectName},
            {label: 'Branch', value: release},
            {label: 'Git SHA', value: gitSHA},
            {label: 'Execution Start Time', value: startTime},
            {label: 'Execution End Time', value: endTime}
        ]
    }
});
