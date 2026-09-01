package md.ramaiana.foodmarket.shared.enums;

/**
 * Order state enum.
 */
public enum OrderState {
  /** The client's cart. At most one per client. */
  NEW,
  /** Submitted by the client, waiting to be handed to the ERP. */
  PLACED,
  /** Written into an orders-data XML file for the ERP to pick up. */
  EXPORTED,
  /**
   * Reserved. Nothing sets these two: the ERP gives no result back, and whatever it decides reaches
   * us through the regular import instead.
   */
  PROCESSED,
  NOT_PROCESSED
}
