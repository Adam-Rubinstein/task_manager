param([int]$interval = 600)

$projectPath = Get-Location
Write-Host "Autocommit started in: $projectPath"
Write-Host "Interval: $interval seconds ($($interval/60) minutes)"
Write-Host "Stop: Ctrl+C"
Write-Host ""

$commitCount = 0

while ($true) {
    try {
        git add -A

        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        $commitMessage = "Auto-commit: $timestamp"

        git commit -m $commitMessage
        git push origin main

        $commitCount++
        Write-Host "Commit #$commitCount pushed to GitHub"
        Write-Host "Next commit time: $(Get-Date -Format 'HH:mm:ss')"
        Write-Host ""
    }
    catch {
        Write-Host "No changes to commit or error: $_"
    }

    Start-Sleep -Seconds $interval
}
