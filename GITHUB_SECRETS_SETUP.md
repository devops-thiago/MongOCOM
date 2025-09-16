# GitHub Secrets Setup for MongOCOM

This document explains how to set up the required GitHub secrets for the MongOCOM project's CI/CD pipeline.

## Required Secrets

The following secrets need to be configured in your GitHub repository settings:

### 1. SONAR_TOKEN (Required for Code Quality Analysis)

**Purpose**: Enables SonarCloud integration for code quality analysis and security scanning.

**How to obtain**:
1. Go to [SonarCloud](https://sonarcloud.io)
2. Log in with your GitHub account
3. Navigate to **My Account** → **Security**
4. Generate a new token with a descriptive name (e.g., "MongOCOM-GitHub-Actions")
5. Copy the generated token

**How to set in GitHub**:
1. Go to your repository: `https://github.com/devops-thiago/MongOCOM`
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Name: `SONAR_TOKEN`
5. Value: Paste the token from SonarCloud
6. Click **Add secret**

### 2. CODECOV_TOKEN (Required for Code Coverage)

**Purpose**: Enables CodeCov integration for code coverage reporting and tracking.

**How to obtain**:
1. Go to [Codecov](https://codecov.io)
2. Log in with your GitHub account
3. Add your repository `devops-thiago/MongOCOM`
4. Go to **Settings** → **Repository Upload Token**
5. Copy the upload token

**How to set in GitHub**:
1. Go to your repository: `https://github.com/devops-thiago/MongOCOM`
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Name: `CODECOV_TOKEN`
5. Value: Paste the token from Codecov
6. Click **Add secret**

### 3. GITHUB_TOKEN (Automatically Available)

**Purpose**: Provides authentication for GitHub API calls within workflows.

**Note**: This token is automatically provided by GitHub Actions and doesn't need to be manually configured.

## SonarCloud Project Configuration

Make sure your SonarCloud project is configured with the following settings:

- **Project Key**: `devops-thiago_MongOCOM`
- **Organization**: `devops-thiago`
- **Repository**: `devops-thiago/MongOCOM`

These settings are already configured in the `pom.xml` file:

```xml
<sonar.organization>devops-thiago</sonar.organization>
<sonar.host.url>https://sonarcloud.io</sonar.host.url>
<sonar.projectKey>devops-thiago_MongOCOM</sonar.projectKey>
<sonar.coverage.jacoco.xmlReportPaths>target/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xml>
```

### ⚠️ **IMPORTANT: Disable Automatic Analysis**

To use CI-based analysis (recommended for better control), you must disable SonarCloud's Automatic Analysis:

1. Go to [SonarCloud](https://sonarcloud.io)
2. Navigate to your project: `devops-thiago_MongOCOM`
3. Go to **Administration** → **Analysis Method**
4. **Disable "Automatic Analysis"**
5. **Enable "CI-based analysis"**
6. Save the changes

**Why CI-based analysis is better:**
- More control over when analysis runs
- Better integration with pull requests
- Consistent with your development workflow
- Includes code coverage from your test suite

## Codecov Project Configuration

Ensure your Codecov project is properly linked:

- **Repository**: `devops-thiago/MongOCOM`
- **Branch**: `master` (primary branch)
- **Coverage Source**: JaCoCo XML reports from `target/site/jacoco/jacoco.xml`

## Workflow Behavior

### With Secrets Configured:
- ✅ SonarCloud analysis runs on every push and PR
- ✅ Code coverage is uploaded to Codecov
- ✅ Quality gates and coverage badges work correctly

### Without Secrets:
- ⚠️ SonarCloud analysis is skipped (workflow continues)
- ⚠️ Codecov upload is skipped (workflow continues)
- ❌ Quality and coverage badges may show "unknown" status

## Testing the Setup

After configuring the secrets:

1. **Push a commit** or **create a PR** to trigger the workflows
2. **Check the Actions tab** to see if workflows complete successfully
3. **Verify SonarCloud** shows the latest analysis
4. **Verify Codecov** shows the latest coverage report
5. **Check README badges** to ensure they display current status

## Troubleshooting

### SonarCloud Issues:

#### "CI analysis while Automatic Analysis is enabled" Error:
```
You are running CI analysis while Automatic Analysis is enabled.
Please consider disabling one or the other.
```
**Solution**:
1. Go to SonarCloud → Your Project → Administration → Analysis Method
2. Disable "Automatic Analysis"
3. Enable "CI-based analysis"
4. Re-run your workflow

#### Other SonarCloud Issues:
- Ensure the token has the correct permissions
- Verify the project key matches exactly (`devops-thiago_MongOCOM`)
- Check that the organization name is correct (`devops-thiago`)
- Verify the repository is correctly linked in SonarCloud

### Codecov Issues:
- Ensure the repository is added to your Codecov account
- Verify the token is copied correctly (no extra spaces)
- Check that JaCoCo reports are being generated (`target/site/jacoco/jacoco.xml`)
- Confirm the repository name matches exactly (`devops-thiago/MongOCOM`)

### Workflow Failures:
- Check the **Actions** tab for detailed error logs
- Ensure all required secrets are set
- Verify secret names match exactly (case-sensitive)
- Check that both SonarCloud and Codecov projects are properly configured

## Security Notes

- **Never commit tokens** to your repository
- **Regenerate tokens** if they are accidentally exposed
- **Use repository secrets** (not environment secrets) for this project
- **Review token permissions** regularly

## Support

For issues with:
- **SonarCloud**: Check [SonarCloud Documentation](https://docs.sonarcloud.io/)
- **Codecov**: Check [Codecov Documentation](https://docs.codecov.com/)
- **GitHub Actions**: Check [GitHub Actions Documentation](https://docs.github.com/en/actions)
