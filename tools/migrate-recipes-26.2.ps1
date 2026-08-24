param(
    [string]$RecipeRoot = (Join-Path $PSScriptRoot "..\src\main\resources\data\createdieselgenerators\recipe")
)

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Convert-IngredientValue {
    param([object]$Value)

    if ($Value -is [System.Array]) {
        return [pscustomobject][ordered]@{
            "neoforge:ingredient_type" = "neoforge:compound"
            children = Convert-RecipeValue $Value
        }
    }
    return Convert-RecipeValue $Value
}

function Convert-RecipeValue {
    param([object]$Value)

    if ($null -eq $Value) {
        return $null
    }
    if ($Value -is [System.Array]) {
        $converted = @()
        foreach ($entry in $Value) {
            $converted += ,(Convert-RecipeValue $entry)
        }
        return ,$converted
    }
    if ($Value -isnot [System.Management.Automation.PSCustomObject]) {
        return $Value
    }

    $properties = @($Value.PSObject.Properties)
    $names = @($properties.Name)

    if ($names.Count -eq 1 -and $names[0] -eq "item") {
        return [string]$Value.item
    }
    if ($names.Count -eq 1 -and $names[0] -eq "tag") {
        return "#" + [string]$Value.tag
    }
    if ($Value.type -eq "fluid_stack") {
        $fluidIngredient = [ordered]@{
            ingredient = [string]$Value.fluid
            amount = [int]$Value.amount
        }
        return [pscustomobject]$fluidIngredient
    }
    if ($Value.type -eq "fluid_tag") {
        $fluidTagIngredient = [ordered]@{
            ingredient = "#" + [string]$Value.fluid_tag
            amount = [int]$Value.amount
        }
        return [pscustomobject]$fluidTagIngredient
    }
    if ($Value.type -eq "neoforge:difference") {
        $difference = [ordered]@{
            "neoforge:ingredient_type" = "neoforge:difference"
            base = Convert-IngredientValue $Value.base
            subtracted = Convert-IngredientValue $Value.subtracted
        }
        return [pscustomobject]$difference
    }
    if ($Value.type -eq "neoforge:compound") {
        $compound = [ordered]@{
            "neoforge:ingredient_type" = "neoforge:compound"
            children = Convert-RecipeValue $Value.children
        }
        return [pscustomobject]$compound
    }
    if ($Value.'neoforge:ingredient_type' -eq "neoforge:difference") {
        $difference = [ordered]@{
            "neoforge:ingredient_type" = "neoforge:difference"
            base = Convert-IngredientValue $Value.base
            subtracted = Convert-IngredientValue $Value.subtracted
        }
        return [pscustomobject]$difference
    }

    $result = [ordered]@{}
    foreach ($property in $properties) {
        if ($property.Name -eq "ingredients" -and $property.Value -is [System.Array]) {
            $ingredients = @()
            foreach ($ingredient in $property.Value) {
                $ingredients += ,(Convert-IngredientValue $ingredient)
            }
            $result[$property.Name] = $ingredients
        } elseif ($property.Name -eq "ingredient") {
            $result[$property.Name] = Convert-IngredientValue $property.Value
        } else {
            $result[$property.Name] = Convert-RecipeValue $property.Value
        }
    }
    return [pscustomobject]$result
}

$files = Get-ChildItem -LiteralPath $RecipeRoot -Recurse -Filter *.json -File
foreach ($file in $files) {
    $raw = Get-Content -LiteralPath $file.FullName -Raw
    $escapedCaseSensitiveKey = $raw -match '"C"\s*:' -and $raw -match '"c"\s*:'
    if ($escapedCaseSensitiveKey) {
        $raw = $raw -replace '"C"\s*:', '"__CDG_UPPERCASE_C__":'
    }
    $json = $raw | ConvertFrom-Json -ErrorAction Stop
    $converted = Convert-RecipeValue $json
    $text = $converted | ConvertTo-Json -Depth 100
    if ($escapedCaseSensitiveKey) {
        $text = $text -replace '"__CDG_UPPERCASE_C__"\s*:', '"C":'
    }
    [System.IO.File]::WriteAllText($file.FullName, $text + [Environment]::NewLine, $utf8NoBom)
}

Write-Output "Migrated $($files.Count) recipe files to the Minecraft 26.2 ingredient format."
