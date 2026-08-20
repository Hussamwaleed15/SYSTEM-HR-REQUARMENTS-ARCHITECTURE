$services = @(
    @{ Name = "Auth Service";         Port = 8081; Endpoint = "http://localhost:8081/api/auth" },
    @{ Name = "Job Service";          Port = 8082; Endpoint = "http://localhost:8082/api/jobs" },
    @{ Name = "Candidate Service";    Port = 8083; Endpoint = "http://localhost:8083/api/candidates" },
    @{ Name = "Application Service";  Port = 8084; Endpoint = "http://localhost:8084/api/applications" },
    @{ Name = "AI Service";           Port = 8085; Endpoint = "http://localhost:8085/api/ai" },
    @{ Name = "Notification Service"; Port = 8086; Endpoint = "http://localhost:8086/api/notifications" }
)

Write-Host "==========================================================================================" -ForegroundColor Cyan
Write-Host "                          HR Recruitment Microservices Status                             " -ForegroundColor Yellow
Write-Host "==========================================================================================" -ForegroundColor Cyan
Write-Host ("{0,-24} {1,-8} {2,-12} {3}" -f "Service Name", "Port", "Status", "Base URL")
Write-Host ("-" * 90)

foreach ($s in $services) {
    $isOpen = $false
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $iar = $tcpClient.BeginConnect("127.0.0.1", $s.Port, $null, $null)
        $wait = $iar.AsyncWaitHandle.WaitOne(600, $false)
        if ($wait -and $tcpClient.Connected) {
            $tcpClient.EndConnect($iar)
            $isOpen = $true
        }
        $tcpClient.Close()
    } catch {
        $isOpen = $false
    }
    
    $status = if ($isOpen) { "ONLINE" } else { "OFFLINE" }
    $color = if ($isOpen) { "Green" } else { "Red" }
    
    Write-Host ("{0,-24} {1,-8} " -f $s.Name, $s.Port) -NoNewline
    Write-Host ("{0,-12} " -f $status) -ForegroundColor $color -NoNewline
    Write-Host $s.Endpoint
}
Write-Host ("-" * 90)
Write-Host "==========================================================================================" -ForegroundColor Cyan
