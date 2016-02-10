# Source: https://gist.github.com/FeodorFitsner/d971c5a98782d211640d
# I have no clue what this mess actually do...

[reflection.assembly]::LoadWithPartialName("Microsoft.SqlServer.Smo") | Out-Null
[reflection.assembly]::LoadWithPartialName("Microsoft.SqlServer.SqlWmiManagement") | Out-Null

$instanceName = $args[0]
$serverName = $env:COMPUTERNAME
$smo = 'Microsoft.SqlServer.Management.Smo.'
$wmi = new-object ($smo + 'Wmi.ManagedComputer')

echo "Enabling TCP/IP for $serverName"
$uri = "ManagedComputer[@Name='$serverName']/ServerInstance[@Name='$instanceName']/ServerProtocol[@Name='Tcp']"
$Tcp = $wmi.GetSmoObject($uri)
$Tcp.IsEnabled = $true
foreach ($ipAddress in $Tcp.IPAddresses) {
    $ipAddress.IPAddressProperties["TcpDynamicPorts"].Value = ""
    $ipAddress.IPAddressProperties["TcpPort"].Value = "1433"
}
$Tcp.Alter()

echo "Enabling named pipes for $serverName"
$uri = "ManagedComputer[@Name='$serverName']/ServerInstance[@Name='$instanceName']/ServerProtocol[@Name='Np']"
$Np = $wmi.GetSmoObject($uri)
$Np.IsEnabled = $true
$Np.Alter()

echo "Setting alias for $serverName"
New-Item HKLM:\SOFTWARE\Microsoft\MSSQLServer\Client -Name ConnectTo | Out-Null
Set-ItemProperty -Path HKLM:\SOFTWARE\Microsoft\MSSQLServer\Client\ConnectTo -Name '(local)' -Value "DBMSSOCN,$serverName\$instanceName" | Out-Null
