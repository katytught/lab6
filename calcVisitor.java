// Generated from .\calc.g4 by ANTLR 4.9.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link calcParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface calcVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link calcParser#compUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompUnit(calcParser.CompUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#funcDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncDef(calcParser.FuncDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecl(calcParser.DeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#constDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstDecl(calcParser.ConstDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#constDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstDef(calcParser.ConstDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#constInitVal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitConstInitVal(calcParser.ConstInitValContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#constExp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitConstExp(calcParser.ConstExpContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#varDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDecl(calcParser.VarDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#varDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDef(calcParser.VarDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#initVal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitInitVal(calcParser.InitValContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(calcParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#blockItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockItem(calcParser.BlockItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(calcParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#cond}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitCond(calcParser.CondContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#lorexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitLorexp(calcParser.LorexpContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#landexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitLandexp(calcParser.LandexpContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#eqexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitEqexp(calcParser.EqexpContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#relexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitRelexp(calcParser.RelexpContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#exp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitExp(calcParser.ExpContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#lval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitLval(calcParser.LvalContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#primaryexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitPrimaryexp(calcParser.PrimaryexpContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#addexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitAddexp(calcParser.AddexpContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#mulexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitMulexp(calcParser.MulexpContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#unaryexp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitUnaryexp(calcParser.UnaryexpContext ctx);
	/**
	 * Visit a parse tree produced by {@link calcParser#funcrParams}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	String visitFuncrParams(calcParser.FuncrParamsContext ctx);
}