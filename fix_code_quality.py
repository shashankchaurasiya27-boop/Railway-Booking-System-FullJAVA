#!/usr/bin/env python3
"""
Fix code quality issues: empty catch blocks and unused imports
"""
import re
from pathlib import Path

BASE_PATH = Path("/Users/shashankchaurasia/Downloads/railway-booking-system copy/src/main/java")

# Files with unused imports to remove
UNUSED_IMPORTS = {
    "com/railway/controller/RegistrationController.java": ["com.railway.booking.backend.util.Constants"],
    "com/railway/view/AdminFrame.java": ["java.awt.event.ActionEvent"],
    "com/railway/view/MainFrame.java": ["java.awt.event.ActionEvent"],
    "com/railway/view/MyBookingsFrame.java": ["java.awt.event.ActionEvent"],
    "com/railway/view/ProfileFrame.java": ["java.awt.event.ActionEvent"],
    "com/railway/view/TrainSearchFrame.java": ["java.awt.event.ActionEvent"],
}

def remove_unused_imports(file_path, imports_to_remove):
    """Remove unused imports from a file"""
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    new_lines = []
    removed_count = 0
    
    for line in lines:
        should_remove = False
        for import_name in imports_to_remove:
            if f'import {import_name};' in line:
                should_remove = True
                removed_count += 1
                break
        
        if not should_remove:
            new_lines.append(line)
    
    if removed_count > 0:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
        print(f"✓ Removed {removed_count} unused import(s) from {file_path.name}")
        return True
    return False

def fix_empty_catch_blocks(file_path):
    """Fix empty catch blocks by adding proper logging"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Pattern to match empty catch blocks
    # Matches: } catch (ExceptionType e) { }
    pattern = r'}\s*catch\s*\(([^)]+)\s+(\w+)\)\s*\{\s*\}'
    
    def replace_catch(match):
        exception_type = match.group(1).strip()
        exception_var = match.group(2).strip()
        
        # Determine appropriate log message based on file type
        if 'DAO.java' in str(file_path):
            log_msg = f'logger.error("Database operation failed: {{}}", {exception_var}.getMessage(), {exception_var});'
        elif 'Service.java' in str(file_path):
            log_msg = f'logger.error("Service operation failed: {{}}", {exception_var}.getMessage(), {exception_var});'
        elif 'Frame.java' in str(file_path) or 'Controller.java' in str(file_path):
            log_msg = f'logger.error("UI operation failed: {{}}", {exception_var}.getMessage(), {exception_var});'
        elif 'Application.java' in str(file_path):
            log_msg = f'logger.warn("Operation failed: {{}}", {exception_var}.getMessage());'
        else:
            log_msg = f'logger.error("Operation failed: {{}}", {exception_var}.getMessage(), {exception_var});'
        
        return f'}} catch ({exception_type} {exception_var}) {{\n            {log_msg}\n        }}'
    
    # Replace all empty catch blocks
    content = re.sub(pattern, replace_catch, content)
    
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        count = len(re.findall(pattern, original_content))
        print(f"✓ Fixed {count} empty catch block(s) in {file_path.name}")
        return True
    return False

def main():
    print("=== Fixing Code Quality Issues ===\n")
    
    # Phase 1: Remove unused imports
    print("Phase 1: Removing Unused Imports")
    print("-" * 60)
    import_fixes = 0
    for rel_path, imports in UNUSED_IMPORTS.items():
        file_path = BASE_PATH / rel_path
        if file_path.exists():
            if remove_unused_imports(file_path, imports):
                import_fixes += 1
    print(f"Total files fixed: {import_fixes}\n")
    
    # Phase 2: Fix empty catch blocks
    print("Phase 2: Fixing Empty Catch Blocks")
    print("-" * 60)
    catch_fixes = 0
    for java_file in sorted(BASE_PATH.rglob("*.java")):
        if fix_empty_catch_blocks(java_file):
            catch_fixes += 1
    print(f"Total files fixed: {catch_fixes}\n")
    
    print("=" * 60)
    print(f"✓ Code quality fixes complete!")
    print(f"  - Removed unused imports from {import_fixes} files")
    print(f"  - Fixed empty catch blocks in {catch_fixes} files")
    print("=" * 60)

if __name__ == "__main__":
    main()
