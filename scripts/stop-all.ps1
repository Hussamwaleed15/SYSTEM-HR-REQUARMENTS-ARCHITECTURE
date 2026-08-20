$ports = @(8081, 8082, 8083, 8084, 8085, 8086)
$stopped = 0
$stoppedPids = @{}

Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "                Stopping HR Recruitment Microservices...                      " -ForegroundColor Yellow
Write-Host "===============================================================================" -ForegroundColor Cyan

foreach ($port in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Where-Object { $_.OwningProcess -gt 0 }
    foreach ($c in $conns) {
        $pidToStop = $c.OwningProcess
        if (-not $stoppedPids.ContainsKey($pidToStop)) {
            try {
                $proc = Get-Process -Id $pidToStop -ErrorAction Stop
                Stop-Process -Id $pidToStop -Force -ErrorAction Stop
                Write-Host ("[STOPPED] Port $port -> Process: $($proc.ProcessName) (PID: $pidToStop)") -ForegroundColor Green
                $stopped++
                $stoppedPids[$pidToStop] = $true
            } catch {
                Write-Host ("[ERROR] Could not stop PID $pidToStop on port $port : $($_.Exception.Message)") -ForegroundColor Red
            }
        }
    }
}

if ($stopped -eq 0) {
    Write-Host "No active services found on ports 8081-8086." -ForegroundColor Gray
} else {
    Write-Host "`nSuccessfully stopped $stopped service process(es)." -ForegroundColor Green
}
Write-Host "===============================================================================" -ForegroundColor Cyan
