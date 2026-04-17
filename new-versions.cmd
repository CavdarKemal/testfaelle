@ECHO OFF
SET RELEASE_VERSION=1.18.2
SET NEW_VERSION=1.3.0
IF NOT "%1"=="" SET NEW_VERSION=%1

ECHO +++++++++++++++++++++++++++++++++++++++++++++  Maven Goal ++++++++++++++++++++++++++++++++++++++++++++
ECHO  mvne ctemvn:auslieferung-maven-plugin:%RELEASE_VERSION%:update-versions -DnewVersion=%NEW_VERSION%
ECHO ---------------------------------------------  Maven Goal --------------------------------------------
REM kurze Schreibweise, weil im POM das Plugin eingetragen wurde: call mvne auslieferung:%RELEASE_VERSION%:update-versions -DnewVersion=%NEW_VERSION%
call mvn ctemvn:auslieferung-maven-plugin:%RELEASE_VERSION%:update-versions -DnewVersion=%NEW_VERSION%

pause