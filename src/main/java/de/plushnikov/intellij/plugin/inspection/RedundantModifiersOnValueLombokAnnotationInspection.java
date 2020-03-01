package de.plushnikov.intellij.plugin.inspection;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.siyeh.ig.fixes.RemoveModifierFix;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

import static com.intellij.psi.PsiModifier.*;

/**
 * @author Rowicki Michał
 */
public class RedundantModifiersOnValueLombokAnnotationInspection extends AbstractBaseJavaLocalInspectionTool {

  @NotNull
  @Override
  public String getGroupDisplayName() {
    return InspectionsBundle.message("group.names.probable.bugs");
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Redundant modifiers on @Value lombok annotations inspection";
  }

  @NotNull
  @Override
  public String getShortName() {
    return "RedundantModifiersValueLombok";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new LombokElementVisitor(holder);
  }

  private static class LombokElementVisitor extends JavaElementVisitor {

    private final ProblemsHolder holder;

    LombokElementVisitor(ProblemsHolder holder) {
      this.holder = holder;
    }

    @Override
    public void visitClass(PsiClass clazz) {
      super.visitClass(clazz);

      if (PsiAnnotationSearchUtil.isAnnotatedWith(clazz, lombok.Value.class)) {
        final PsiModifierList modifierList = clazz.getModifierList();
        if (modifierList != null && modifierList.hasExplicitModifier(FINAL)) {
          final Optional<PsiElement> psiFinal = Arrays.stream(modifierList.getChildren())
            .filter(psiElement -> FINAL.equals(psiElement.getText()))
            .findFirst();

          psiFinal.ifPresent(psiElement -> holder.registerProblem(psiElement,
            "Redundant final class modifier",
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            new RemoveModifierFix(FINAL))
          );
        }
      }
    }

    @Override
    public void visitField(PsiField field) {
      super.visitField(field);
      final PsiModifierList modifierList = field.getModifierList();
      if (modifierList != null && !modifierList.hasExplicitModifier(STATIC)) {
        final PsiElement parent = field.getParent();
        if (parent instanceof PsiClass) {
          final PsiClass clazz = (PsiClass) parent;
          if (PsiAnnotationSearchUtil.isAnnotatedWith(clazz, lombok.Value.class)) {
            if (modifierList.hasExplicitModifier(PRIVATE)) {
              final Optional<PsiElement> psiPrivate = Arrays.stream(modifierList.getChildren())
                .filter(psiElement -> PRIVATE.equals(psiElement.getText()))
                .findFirst();

              psiPrivate.ifPresent(psiElement -> holder.registerProblem(psiElement,
                "Redundant private field modifier",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                new RemoveModifierFix(PRIVATE))
              );
            }
            if (modifierList.hasExplicitModifier(FINAL)) {
              final Optional<PsiElement> psiFinal = Arrays.stream(modifierList.getChildren())
                .filter(psiElement -> FINAL.equals(psiElement.getText()))
                .findFirst();

              psiFinal.ifPresent(psiElement -> holder.registerProblem(psiElement,
                "Redundant final field modifier",
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                new RemoveModifierFix(FINAL))
              );
            }
          }
        }
      }
    }

  }
}
