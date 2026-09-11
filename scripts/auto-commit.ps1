# ==============================================================================
# Auto-Commit Watcher for EMS Portal
# Automatically commits code changes to Git when files are edited and saved.
# ==============================================================================

param(
    [int]$DebounceSeconds = 5,
    [string]$Branch = "main"
)

$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot

Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  EMS Portal — Git Auto-Commit Watcher Active         " -ForegroundColor Green
Write-Host "  Watching directory: $RepoRoot" -ForegroundColor Yellow
Write-Host "  Debounce delay    : $DebounceSeconds seconds" -ForegroundColor Yellow
Write-Host "  Press Ctrl+C at any time to stop." -ForegroundColor Gray
Write-Host "======================================================" -ForegroundColor Cyan

# Ensure git is available
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "Git is not found on PATH. Please ensure Git is installed."
    exit 1
}

while ($true) {
    Start-Sleep -Seconds $DebounceSeconds
    
    # Check if there are unstaged, staged, or untracked changes
    $status = git status --porcelain
    if ($status) {
        $changeCount = ($status | Measure-Object).Count
        $timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
        
        Write-Host "[$timestamp] Detected $changeCount changed file(s). Staging and committing..." -ForegroundColor Magenta
        
        git add .
        $commitMessage = "auto(dev): update code changes at $timestamp"
        $commitResult = git commit -m "$commitMessage" 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[$timestamp] Successfully committed: $commitMessage" -ForegroundColor Green
        } else {
            Write-Host "[$timestamp] Commit skipped or nothing to commit." -ForegroundColor DarkGray
        }
    }
}
