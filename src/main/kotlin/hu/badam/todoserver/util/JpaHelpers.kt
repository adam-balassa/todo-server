package hu.badam.todoserver.util

import org.hibernate.Criteria
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

inline fun <reified ResultT> criteriaQuery(
        em: EntityManager,
        buildQuery: CriteriaQuery<ResultT>.(root: Root<ResultT>, cb: CriteriaBuilder, cr: CriteriaQuery<ResultT>) -> Unit
): List<ResultT> {

    val cb = em.criteriaBuilder
    val cr = cb.createQuery(ResultT::class.java)
    val root = cr.from(ResultT::class.java)

    cr.buildQuery(root, cb, cr)

    val query = em.createQuery(cr)
    return query.resultList
}

inline fun ifNotNull(obj: Any?, cb: CriteriaBuilder, predicateBuilder: (obj: Any) -> Predicate): Predicate {
    return if (obj != null) predicateBuilder(obj)
    else cb.isTrue(cb.literal(true))
}