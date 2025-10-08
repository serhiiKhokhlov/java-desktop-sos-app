$containerRuns = $(docker ps -f "name=^/mysql$" --format "{{.Names}}").length -gt 0
$containerExists = $containerRuns -or ($(docker ps -a -f "name=^/mysql$" --format "{{.Names}}").length -gt 0)
$networkName = "sos-net"
$networkExists = docker network ls --format "{{.Name}}" | Select-String -Pattern $networkName -Quiet

if (-not $networkExists) {
  Write-Host -ForegroundColor Yellow "creating network '$networkName' ..."
  docker network create $networkName | Out-Null
}
if ($containerRuns) {
  Write-Host -ForegroundColor Yellow "MySQL server is already running."
  exit 0
}
elseif ($containerExists) {
  Write-Host -ForegroundColor Yellow "starting existing MySQL container ..."
  docker start mysql | Out-Null
}
else {
  Write-Host -ForegroundColor Yellow "creating and starting MySQL container ..."
  docker run --name mysql -d -p 3306:3306 -e "MYSQL_ALLOW_EMPTY_PASSWORD=1" --network $networkName mysql:9 2>&1 | Out-Null
}
