#!/bin/bash

# SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

# Simple PKGBUILD integrity checker
# Verifies that source files match their sha256sums

PKGBUILD_FILE="${1:-PKGBUILD}"

if [[ ! -f "$PKGBUILD_FILE" ]]; then
    echo "Error: PKGBUILD file not found: $PKGBUILD_FILE"
    echo "Usage: $0 [PKGBUILD_file]"
    exit 1
fi

# Get the directory containing the PKGBUILD and change to it
PKGBUILD_DIR=$(dirname "$PKGBUILD_FILE")
PKGBUILD_NAME=$(basename "$PKGBUILD_FILE")

echo "Checking integrity of $PKGBUILD_FILE"
echo "Working directory: $(realpath "$PKGBUILD_DIR")"
echo "=================================="

# Change to PKGBUILD directory
pushd "$PKGBUILD_DIR" > /dev/null || {
    echo "Error: Cannot change to directory $PKGBUILD_DIR"
    exit 1
}

# shellcheck source=/dev/null
source "./$PKGBUILD_NAME" || {
    echo "Error: Cannot source PKGBUILD file"
    popd > /dev/null || exit
    exit 1
}

# Check if source and sha256sums arrays exist
if [[ -z "${source[*]:-}" ]] || [[ ${#source[@]} -eq 0 ]]; then
    echo "Error: No source array found in PKGBUILD"
    popd > /dev/null || exit
    exit 1
fi

if [[ -z "${sha256sums[*]:-}" ]] || [[ ${#sha256sums[@]} -eq 0 ]]; then
    echo "Error: No sha256sums array found in PKGBUILD"
    popd > /dev/null || exit
    exit 1
fi

# Get array lengths
source_count=${#source[@]}
sums_count=${#sha256sums[@]}

echo "Sources found: $source_count"
echo "SHA256 sums found: $sums_count"
echo

# Check if counts match
if [[ $source_count -ne $sums_count ]]; then
    echo "❌ ERROR: Mismatch in counts!"
    echo "   Sources: $source_count"
    echo "   SHA256sums: $sums_count"
    popd > /dev/null || exit
    exit 1
fi

# Check each source file
errors=0
for i in "${!source[@]}"; do
    src="${source[$i]}"
    expected_sum="${sha256sums[$i]}"

    # Extract filename from source (handle URLs)
    if [[ "$src" =~ ^https?:// ]]; then
        filename=$(basename "$src")
        echo "[$((i+1))/$source_count] URL: $filename"
        if [[ "$expected_sum" == "SKIP" ]]; then
            echo "   ✅ SKIP - integrity check disabled"
        else
            echo "   ⚠️  URL - cannot verify without downloading"
        fi
    else
        # Local file - should be in current directory (PKGBUILD dir)
        filename="$src"
        echo "[$((i+1))/$source_count] File: $filename"

        if [[ "$expected_sum" == "SKIP" ]]; then
            echo "   ✅ SKIP - integrity check disabled"
        elif [[ -f "$filename" ]]; then
            # Calculate actual hash
            actual_sum=$(sha256sum "$filename" | cut -d' ' -f1)

            if [[ "$actual_sum" == "$expected_sum" ]]; then
                echo "   ✅ OK - hash matches"
            else
                echo "   ❌ FAIL - hash mismatch!"
                echo "      Expected: $expected_sum"
                echo "      Actual:   $actual_sum"
                ((errors++))
            fi
        else
            echo "   ❌ FAIL - file not found!"
            ((errors++))
        fi
    fi
    echo
done

# Return to original directory
popd > /dev/null || exit

# Final result
echo "=================================="
if [[ $errors -eq 0 ]]; then
    echo "✅ All integrity checks passed!"
    exit 0
else
    echo "❌ $errors integrity check(s) failed!"
    exit 1
fi
