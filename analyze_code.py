#!/usr/bin/env python3
"""
Analyze Java files for common issues that cause IDE warnings
"""
import os
import re
from pathlib import Path
from collections import defaultdict

BASE_PATH = Path("/Users/shashankchaurasia/Downloads/railway-booking-system copy/src/main/java")

issues = defaultdict(list)

def analyze_file(file_path):
    """Analyze a single Java file for common issues"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        lines = content.split('\n')
    
    file_issues = []
    
    # Check for unused imports
    imports = []
    for i, line in enumerate(lines, 1):
        if line.strip().startswith('import ') and not line.strip().startswith('import static'):
            import_match = re.search(r'import\s+([\w.]+);', line)
            if import_match:
                imported_class = import_match.group(1).split('.')[-1]
                # Check if class is used in the file
                if content.count(imported_class) == 1:  # Only appears in import
                    file_issues.append(f"Line {i}: Unused import: {import_match.group(1)}")
    
    # Check for unused variables
    var_pattern = r'\b(\w+)\s+(\w+)\s*='
    for match in re.finditer(var_pattern, content):
        var_name = match.group(2)
        # Count occurrences (should be more than 1 if used)
        if content.count(var_name) == 1:
            file_issues.append(f"Potentially unused variable: {var_name}")
    
    # Check for missing @Override annotations
    method_pattern = r'(public|protected|private)\s+\w+\s+\w+\s*\([^)]*\)\s*\{'
    for i, line in enumerate(lines, 1):
        if re.search(method_pattern, line):
            # Check if previous line has @Override
            if i > 1 and '@Override' not in lines[i-2]:
                # Common overridden methods
                if any(method in line for method in ['toString', 'equals', 'hashCode', 'doInBackground', 'done']):
                    file_issues.append(f"Line {i}: Missing @Override annotation")
    
    # Check for empty catch blocks
    for i, line in enumerate(lines, 1):
        if 'catch' in line and i < len(lines) - 1:
            next_line = lines[i].strip() if i < len(lines) else ''
            if next_line == '}' or (i < len(lines) - 1 and lines[i+1].strip() == '}'):
                file_issues.append(f"Line {i}: Empty catch block")
    
    # Check for System.out/err usage (should use logger)
    for i, line in enumerate(lines, 1):
        if 'System.out.print' in line or 'System.err.print' in line:
            file_issues.append(f"Line {i}: Using System.out/err instead of logger")
    
    return file_issues

def analyze_all_files():
    """Analyze all Java files"""
    print("=== Code Analysis Report ===\n")
    
    total_issues = 0
    files_with_issues = 0
    
    for java_file in sorted(BASE_PATH.rglob("*.java")):
        file_issues = analyze_file(java_file)
        if file_issues:
            files_with_issues += 1
            total_issues += len(file_issues)
            rel_path = java_file.relative_to(BASE_PATH)
            print(f"\n{rel_path}")
            print("-" * 60)
            for issue in file_issues[:10]:  # Limit to 10 issues per file
                print(f"  • {issue}")
            if len(file_issues) > 10:
                print(f"  ... and {len(file_issues) - 10} more issues")
    
    print(f"\n{'='*60}")
    print(f"Total files analyzed: {len(list(BASE_PATH.rglob('*.java')))}")
    print(f"Files with issues: {files_with_issues}")
    print(f"Total issues found: {total_issues}")
    print(f"{'='*60}")

if __name__ == "__main__":
    analyze_all_files()
